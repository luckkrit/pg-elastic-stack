package pgdemo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.FloatNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.IntegerNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class App {

    private static final String PG_URL = System.getenv().getOrDefault(
            "PG_URL", "jdbc:postgresql://127.0.0.1:5432/postgres");
    private static final String PG_USERNAME = System.getenv().getOrDefault("PG_USERNAME", "postgres");
    private static final String PG_PASSWORD = System.getenv().getOrDefault("PG_PASSWORD", "password");
    private static final String ES_URL = System.getenv().getOrDefault("ES_URL", "http://localhost:9200");

    // ---------- Step 0: mapping definition ----------

    // NOTE: keys must be lowercase — fetchRows() lowercases every JDBC column name
    // (meta.getColumnName(i).toLowerCase()), so a mapping key like "productCode" would
    // never match the actual document field "productcode" and would silently never apply.
    public static Map<String, Property> getProductMapping() {
        Map<String, Property> properties = new LinkedHashMap<>();
        properties.put("productcode", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        properties.put("productname", new Property.Builder()
                .text(new TextProperty.Builder().build())
                .build());
        properties.put("productline", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        properties.put("productdescription", new Property.Builder()
                .text(new TextProperty.Builder().build())
                .build());
        properties.put("productscale", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        properties.put("productvendor", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        properties.put("buyprice", new Property.Builder()
                .float_(new FloatNumberProperty.Builder().build())
                .build());
        properties.put("msrp", new Property.Builder()
                .float_(new FloatNumberProperty.Builder().build())
                .build());
        properties.put("quantityinstock", new Property.Builder()
                .integer(new IntegerNumberProperty.Builder().build())
                .build());
        return properties;
    }

    public static Map<String, Property> getCustomerMapping() {
        Map<String, Property> properties = new LinkedHashMap<>();
        properties.put("customernumber", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        properties.put("customername", new Property.Builder()
                .text(new TextProperty.Builder().build())
                .build());
        properties.put("city", new Property.Builder()
                .text(new TextProperty.Builder().build())
                .build());
        properties.put("country", new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build());
        return properties;
    }

    // ---------- Step 1: read from PostgreSQL ----------

    /** Runs SELECT * on tableName and returns one Map per row, column name -> normalized value. */
    private static List<Map<String, Object>> fetchRows(String tableName) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = DriverManager.getConnection(PG_URL, PG_USERNAME, PG_PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String colName = meta.getColumnName(i).toLowerCase();
                    row.put(colName, normalize(rs.getObject(i)));
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }

        return rows;
    }

    /** Convert JDBC types that don't serialize to JSON the way we want (dates, non-finite floats). */
    private static Object normalize(Object value) {
        if (value instanceof java.sql.Date d) return d.toLocalDate().toString();
        if (value instanceof java.sql.Timestamp t) return t.toLocalDateTime().toString();
        if (value instanceof java.math.BigDecimal) return value;
        if (value instanceof Double d && (d.isNaN() || d.isInfinite())) return null;
        if (value instanceof Float f && (f.isNaN() || f.isInfinite())) return null;
        // PostGIS columns (e.g. customerlocation) come back as PGobject via the plain postgresql
        // driver — Jackson can't serialize that class, so pull out its raw string value (the
        // WKB/EWKB hex representation) instead of passing the driver object straight through.
        if (value instanceof org.postgresql.util.PGobject pg) return pg.getValue();
        return value;
    }

    // ---------- Step 2: build the bulk request ----------

    /** Joins the primary-key column values with "_" to form a stable document _id. */
    private static String buildDocId(Map<String, Object> row, List<String> pkCols) {
        StringBuilder sb = new StringBuilder();
        for (String col : pkCols) {
            if (sb.length() > 0) sb.append("_");
            sb.append(row.get(col));
        }
        return sb.toString();
    }

    /** Wraps every row into an IndexOperation and packs them all into one BulkRequest. */
    private static BulkRequest buildBulkRequest(String indexName, List<String> pkCols,
            List<Map<String, Object>> rows) {

        List<BulkOperation> operations = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String docId = buildDocId(row, pkCols);

            IndexOperation<Map<String, Object>> indexOp = new IndexOperation.Builder<Map<String, Object>>()
                    .index(indexName)
                    .id(docId)
                    .document(row)
                    .build();

            BulkOperation bulkOp = new BulkOperation.Builder()
                    .index(indexOp)
                    .build();

            operations.add(bulkOp);
        }

        return new BulkRequest.Builder()
                .operations(operations)
                .build();
    }

    // ---------- Step 3: create the index if it doesn't exist yet ----------

    /** Creates indexName with the given mapping, unless it already exists. */
    private static void ensureIndex(ElasticsearchClient esClient, String indexName,
            Map<String, Property> properties) throws IOException {

        ExistsRequest existsRequest = new ExistsRequest.Builder()
                .index(indexName)
                .build();
        boolean exists = esClient.indices().exists(existsRequest).value();

        if (exists) {
            System.out.println("  index '" + indexName + "' already exists — skipping mapping creation");
            return;
        }

        TypeMapping mapping = new TypeMapping.Builder()
                .properties(properties)
                .build();

        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(indexName)
                .mappings(mapping)
                .build();

        esClient.indices().create(request);
        System.out.println("  ✓ created index '" + indexName + "' with explicit mapping");
    }

    // ---------- Step 4: send the bulk request and report the result ----------

    private static void printBulkResult(BulkResponse result, int rowCount, String indexName) {
        if (!result.errors()) {
            System.out.println("  ✓ " + rowCount + " documents indexed into '" + indexName + "'");
            return;
        }
        for (BulkResponseItem item : result.items()) {
            if (item.error() != null) {
                System.err.println("Failed: " + item.error().reason());
            }
        }
    }

    // ---------- Orchestration: run all the steps above in order ----------

    public static void migrateTable(String indexName, String tableName, List<String> pkCols,
            Map<String, Property> properties) {

        List<Map<String, Object>> rows = fetchRows(tableName);
        BulkRequest bulkRequest = buildBulkRequest(indexName, pkCols, rows);

        HttpHost host = HttpHost.create(ES_URL);

        try (RestClient restClient = RestClient.builder(host).build()) {
            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient esClient = new ElasticsearchClient(transport);

            ensureIndex(esClient, indexName, properties);

            BulkResponse result = esClient.bulk(bulkRequest);
            printBulkResult(result, rows.size(), indexName);

        } catch (IOException e) {
            System.err.println("Elasticsearch error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        migrateTable("products", "classicmodels.products", List.of("productcode"), getProductMapping());
        migrateTable("customers", "classicmodels.customers", List.of("customernumber"), getCustomerMapping());
    }

}