package com.example.migrate;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

public class ProductSearchMapper extends DocumentMapper {

    public ProductSearchMapper(String pgUrl, String pgUsername, String pgPassword, String esUrl) {
        super(pgUrl, pgUsername, pgPassword, esUrl);
    }

    @Override
    public void migrate() throws SQLException, IOException {
        processQuery("select * from classicmodels.products_search_view", new ResultSetHandler() {
            @Override
            public void handle(ResultSet rs) {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    try {
                        if (!rs.next())
                            break;
                        String productCode = rs.getString("productcode");
                        String productName = rs.getString("productname");
                        String productLine = rs.getString("productline");
                        String productDescription = rs.getString("productdescription");
                        String productLineDescription = rs.getString("productlinedescription");

                        sb.append(compactJson("""
                                {"index":{"_id": %s}}
                                        """.formatted(jsonValue(productCode)).strip()));
                        sb.append("\n");

                        // Compact JSON representation of the product document
                        sb.append(compactJson("""
                                {
                                    "productcode": %s,
                                    "productname": %s,
                                    "productline": %s,
                                    "productdescription": %s,
                                    "productlinedescription": %s
                                }
                                """.formatted(
                                jsonValue(productCode),
                                jsonValue(productName),
                                jsonValue(productLine),
                                jsonValue(productDescription),
                                jsonValue(productLineDescription)

                        )));

                        sb.append('\n');
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                connectEs(new EsClientHandler() {
                    @Override
                    public void handle(Rest5Client client) throws SQLException, IOException {
                        String indexName = "products_search";
                        String productSearchIndex = """
                                {
                                  "mappings": {
                                    "properties": {
                                      "productcode":            { "type": "keyword" },
                                      "productname":            { "type": "text" },
                                      "productline":            { "type": "keyword" },
                                      "productdescription":     { "type": "text" },
                                      "productlinedescription": { "type": "text" }
                                    }
                                  }
                                }

                                                                """;
                        createIndex(client, indexName, productSearchIndex);
                        bulkInsert(client, indexName, sb.toString());
                    }

                });
            }
        });
    }

}
