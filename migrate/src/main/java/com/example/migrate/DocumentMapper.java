package com.example.migrate;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import co.elastic.clients.transport.rest5_client.low_level.Request;
import co.elastic.clients.transport.rest5_client.low_level.Response;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public abstract class DocumentMapper {

    private static final JsonMapper mapper = new JsonMapper();

    private final String pgUrl;
    private final String pgUsername;
    private final String pgPassword;
    private final String esUrl;

    protected DocumentMapper(
            String pgUrl,
            String pgUsername,
            String pgPassword,
            String esUrl) {

        this.pgUrl = pgUrl;
        this.pgUsername = pgUsername;
        this.pgPassword = pgPassword;
        this.esUrl = esUrl;
    }

    protected void createIndex(Rest5Client client, String indexName, String indexMapping) throws IOException {
        Request request = new Request(
                "PUT",
                "/" + indexName);
        request.setJsonEntity(indexMapping);
        Response response = client.performRequest(request);

        int status = response.getStatusCode();

        String responseBody = new String(
                response.getEntity()
                        .getContent()
                        .readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);

        System.out.println("HTTP Status: " + status);
        System.out.println("Response: " + responseBody);

        if (status < 200 || status >= 300) {
            throw new IOException(
                    "Bulk Insert failed. HTTP "
                            + status
                            + "\n"
                            + responseBody);
        }
    }

    protected interface ResultSetHandler {

        void handle(ResultSet rs)
                throws SQLException, IOException;
    }

    protected void processQuery(
            String sql,
            ResultSetHandler handler)
            throws SQLException, IOException {

        try (
                Connection conn = DriverManager.getConnection(
                        pgUrl,
                        pgUsername,
                        pgPassword);

                Statement stmt = conn.createStatement();

                ResultSet rs = stmt.executeQuery(sql)) {

            handler.handle(rs);
        }
    }

    protected void bulkInsert(
            Rest5Client client,
            String indexName,
            String ndjson) throws IOException {

        // 1. NDJSON must end with newline
        if (!ndjson.endsWith("\n")) {
            ndjson += "\n";
        }

        // 2. Create Bulk Request
        Request request = new Request(
                "POST",
                "/" + indexName + "/_bulk");

        String[] lines = ndjson.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            System.out.printf(
                    "Line %d: [%s]%n",
                    i + 1,
                    lines[i]);
        }
        request.setJsonEntity(ndjson);
        // Set request entity with application/x-ndjson
        // using the entity API of your Rest5Client version.

        // 3. Execute Bulk Request
        Response response = client.performRequest(request);

        // 4. Check HTTP status
        int status = response.getStatusCode();

        String responseBody = new String(
                response.getEntity()
                        .getContent()
                        .readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);

        System.out.println("Bulk HTTP Status: " + status);
        System.out.println("Bulk Response: " + responseBody);

        if (status < 200 || status >= 300) {
            throw new IOException(
                    "Bulk Insert failed. HTTP "
                            + status
                            + "\n"
                            + responseBody);
        }

    }

    protected String jsonValue(Object value) {
        return mapper.writeValueAsString(value);
    }

    protected String compactJson(String json) {
        JsonNode node = mapper.readTree(json);
        return mapper.writeValueAsString(node);
    }

    protected interface EsClientHandler {

        void handle(Rest5Client client)
                throws SQLException, IOException;
    }

    protected void connectEs(EsClientHandler handler) {
        try (Rest5Client client = Rest5Client.builder(
                URI.create(esUrl)).build()) {
            handler.handle(client);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void migrate() throws SQLException, IOException {
    }
}
