# classicmodels: PostgreSQL → Elasticsearch pipeline

Docker Compose stack that hosts the `classicmodels` sample database in
PostgreSQL (with PostGIS), lets you browse/query it with pgAdmin, and
migrates it into Elasticsearch for exploration in Kibana.

## Services

| Service        | Image                                    | Port(s)      | Purpose                                      |
|----------------|-------------------------------------------|--------------|-----------------------------------------------|
| `elasticsearch`| `docker.elastic.co/elasticsearch/elasticsearch:9.5.3` | `9200`, `5601` | Search/analytics engine (security disabled) |
| `kibana`       | `docker.elastic.co/kibana/kibana:9.5.3`   | shares `elasticsearch`'s network | Web UI for Elasticsearch |
| `postgres`     | `postgis/postgis:16-3.4`                  | `5432`, `5050` | Source database (`classicmodels` schema) + PostGIS |
| `pgadmin`      | `dpage/pgadmin4:latest`                   | shares `postgres`'s network | Web UI for PostgreSQL |
| `migrate`      | built from local `Dockerfile`             | shares host network | One-shot job: copies Postgres tables into Elasticsearch indices |

## Prerequisites

- Docker + Docker Compose
- A `classicmodels.sql` dump, placed at `./initdb/classicmodels.sql`
  (this runs automatically the **first time** the `postgres` volume is
  created — see Troubleshooting if you add it later)

## Project layout

```
.
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── initdb/
│   └── classicmodels.sql
└── src/main/java/pgdemo/
    └── App.java
```

## Getting started

**1. Start the core services** (Elasticsearch, Kibana, Postgres, pgAdmin):

```bash
docker compose up -d
```

Elasticsearch takes 20–60s to become healthy; Kibana waits behind it.

**2. Check everything is up:**

```bash
docker ps
```

**3. Run the migration** (Postgres → Elasticsearch). This is a one-shot
job, not started automatically by `up`:

```bash
docker compose run --rm migrate
```

**4. Verify the data landed in Elasticsearch:**

```bash
curl http://localhost:9200/_cat/indices?v
```

## Accessing each service

| What | URL | Credentials |
|---|---|---|
| Kibana | http://localhost:5601 | — (security disabled) |
| Elasticsearch API | http://localhost:9200 | — (security disabled) |
| pgAdmin | http://localhost:5050 | `admin@admin.com` / `admin` |
| PostgreSQL (direct) | `localhost:5432`, db `postgres` | `postgres` / `password` |

> Running in GitHub Codespaces? Open these via the **Ports** tab in
> VS Code (click the globe icon) rather than typing the URL — the
> forwarded domain differs from `localhost`.

## Configuration notes

- **Memory limits** (`mem_limit`, `ES_JAVA_OPTS`, `NODE_OPTIONS`) are
  tuned for an 8GB machine. On a machine with less RAM, lower them
  further; below ~512MB heap, Kibana can fail to boot (it loads ~200
  plugins on startup).
- **PostGIS**: the `postgres` service uses the `postgis/postgis` image,
  which auto-runs `CREATE EXTENSION postgis` on first init. Verify with:
  ```bash
  docker exec -it postgres psql -U postgres -d postgres -c "SELECT PostGIS_version();"
  ```
- **`migrate` connection settings** are configurable via environment
  variables (`PG_URL`, `PG_USERNAME`, `PG_PASSWORD`, `ES_URL`) — see
  `docker-compose.yml`. Defaults assume everything runs together in
  this compose file.

## Troubleshooting

**Container-to-container connections time out (e.g. `migrate` can't
reach `postgres`, or `kibana` can't reach `elasticsearch`).**
In some Docker-in-Docker environments (notably GitHub Codespaces),
traffic between containers on a bridge network doesn't route
correctly. The fix used throughout this compose file:
- `kibana` shares `elasticsearch`'s network (`network_mode:
  "service:elasticsearch"`) and connects via `localhost`.
- `pgadmin` shares `postgres`'s network the same way.
- `migrate` uses `network_mode: "host"` and connects via `localhost`
  to the published ports.
This is why ports for `kibana`/`pgadmin` are declared on the service
they share a network with, not on themselves.

**`docker exec -it postgres psql ... "\dt classicmodels.*"` shows no
tables, even though the schema exists.**
The init script in `./initdb/` only runs the **first time** the
`postgres` data volume is created. If you add/change the `.sql` file
afterward, it won't re-run automatically. Reset with:
```bash
docker compose down
docker volume rm <project>_pgdata
docker compose up -d postgres
```

**pgAdmin shows a blank page stuck on "Loading pgAdmin 4..." or throws
`KeyError: 'auth_source_manager'`.**
This happens when switching between pgAdmin's "desktop mode"
(`PGADMIN_CONFIG_SERVER_MODE=False`) and normal login mode — old
session data in the `pgadmin_data` volume doesn't match the new mode's
session format. Fix: stop pgAdmin, remove the container and the
`pgadmin_data` volume, then start it fresh, and open it in a **new**
private/incognito window (old cookies can also cause this).
```bash
docker compose stop pgadmin
docker rm pgadmin
docker volume rm <project>_pgadmin_data
docker compose up -d pgadmin
```
Desktop mode is also known to behave unreliably behind a reverse proxy
(like the Codespaces port-forwarding domain) — normal login mode is
more reliable in that environment.

**Only one container shows up after `docker compose up -d`.**
Check that you didn't pass a specific service name (e.g. `docker
compose up -d postgres` only starts `postgres`). Run `docker compose
up -d` with no service name to start everything.

**Kibana takes 1–3 minutes to become reachable.**
Normal — it initializes ~200 plugins on every startup. On Windows,
add a Windows Defender exclusion for the install folder if running
natively (not Docker); antivirus scanning of the many plugin files
slows this down significantly.