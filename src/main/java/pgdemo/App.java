package pgdemo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class App {

    // ---------- PostgreSQL connection ----------
    // Falls back to localhost defaults (for running outside Docker);
    // inside docker-compose, these get overridden via environment variables.
    static final String PG_URL = System.getenv().getOrDefault(
            "PG_URL", "jdbc:postgresql://localhost:5432/postgres");
    static final String PG_USERNAME = System.getenv().getOrDefault("PG_USERNAME", "postgres");
    static final String PG_PASSWORD = System.getenv().getOrDefault("PG_PASSWORD", "password");

    // ---------- Elasticsearch ----------
    static final String ES_URL = System.getenv().getOrDefault(
            "ES_URL", "http://localhost:9200");

    static final int BATCH_SIZE = 500;

    // schema-qualified table name -> (primary key columns, ES index name)
    static final Map<String, TableConfig> TABLES = new LinkedHashMap<>();
    static {
        TABLES.put("classicmodels.customers", new TableConfig(List.of("customernumber"), "customers"));
        TABLES.put("classicmodels.employees", new TableConfig(List.of("employeenumber"), "employees"));
        TABLES.put("classicmodels.offices", new TableConfig(List.of("officecode"), "offices"));
        TABLES.put("classicmodels.orderdetails", new TableConfig(List.of("ordernumber", "productcode"), "orderdetails"));
        TABLES.put("classicmodels.orders", new TableConfig(List.of("ordernumber"), "orders"));
        TABLES.put("classicmodels.payments", new TableConfig(List.of("customernumber", "checknumber"), "payments"));
        TABLES.put("classicmodels.productlines", new TableConfig(List.of("productline"), "productlines"));
        TABLES.put("classicmodels.products", new TableConfig(List.of("productcode"), "products"));
    }

    record TableConfig(List<String> pkCols, String indexName) {}

    static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(PG_URL, PG_USERNAME, PG_PASSWORD)) {

            System.out.println("Connected to PostgreSQL database!");

            for (Map.Entry<String, TableConfig> entry : TABLES.entrySet()) {
                migrateTable(conn, entry.getKey(), entry.getValue());
            }

            System.out.println("\nDone. Verify with:");
            System.out.println("  curl \"" + ES_URL + "/_cat/indices?v\"");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Migration error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void migrateTable(Connection conn, String tableName, TableConfig config) throws Exception {
        System.out.println("Migrating '" + tableName + "' -> index '" + config.indexName() + "'...");

        String sql = "SELECT * FROM " + tableName;
        int total = 0;
        List<Map<String, Object>> batch = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String colName = meta.getColumnName(i).toLowerCase();
                    row.put(colName, normalize(rs.getObject(i)));
                }
                batch.add(row);

                if (batch.size() >= BATCH_SIZE) {
                    bulkUpload(config.indexName(), config.pkCols(), batch);
                    total += batch.size();
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            bulkUpload(config.indexName(), config.pkCols(), batch);
            total += batch.size();
        }

        System.out.println("  \u2713 " + total + " documents indexed into '" + config.indexName() + "'");
    }

    /** Convert JDBC types that don't serialize to JSON directly (dates, BigDecimal). */
    static Object normalize(Object value) {
        if (value instanceof java.sql.Date d) return d.toLocalDate().toString();
        if (value instanceof java.sql.Timestamp t) return t.toLocalDateTime().toString();
        if (value instanceof java.math.BigDecimal bd) return bd.doubleValue();
        return value;
    }

    static void bulkUpload(String indexName, List<String> pkCols, List<Map<String, Object>> rows) throws Exception {
        StringBuilder payload = new StringBuilder();

        for (Map<String, Object> row : rows) {
            String docId = buildDocId(row, pkCols);
            payload.append("{\"index\":{\"_index\":\"").append(indexName)
                   .append("\",\"_id\":\"").append(escape(docId)).append("\"}}\n");
            payload.append(toJson(row)).append("\n");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ES_URL + "/_bulk"))
                .header("Content-Type", "application/x-ndjson")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            System.err.println("  \u26a0 Bulk request failed: HTTP " + response.statusCode());
            System.err.println(response.body());
        } else if (response.body().contains("\"errors\":true")) {
            System.err.println("  \u26a0 Some documents in this batch failed to index");
        }
    }

    static String buildDocId(Map<String, Object> row, List<String> pkCols) {
        StringBuilder sb = new StringBuilder();
        for (String col : pkCols) {
            if (sb.length() > 0) sb.append("_");
            sb.append(row.get(col));
        }
        return sb.toString();
    }

    /** Build a JSON object string from a row map — no library needed. */
    static String toJson(Map<String, Object> row) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            sb.append(jsonValue(e.getValue()));
        }
        return sb.append("}").toString();
    }

    static String jsonValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        return "\"" + escape(value.toString()) + "\"";
    }

    static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}