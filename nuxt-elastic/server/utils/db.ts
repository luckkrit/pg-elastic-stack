import pg from 'pg'

const { Pool } = pg

let pool: pg.Pool | undefined

export function useDb() {
    if (!pool) {
        const config = useRuntimeConfig()

        pool = new Pool({
            connectionString: config.databaseUrl
        })
    }

    return pool
}