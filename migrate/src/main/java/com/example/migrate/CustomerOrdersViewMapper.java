package com.example.migrate;

import java.io.IOException;
import java.sql.SQLException;

import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

public class CustomerOrdersViewMapper extends DocumentMapper {

    public CustomerOrdersViewMapper(String pgUrl, String pgUsername, String pgPassword, String esUrl) {
        super(pgUrl, pgUsername, pgPassword, esUrl);
    }

    @Override
    public void migrate() throws java.sql.SQLException, java.io.IOException {
        processQuery("select * from classicmodels.customer_orders_view", new ResultSetHandler() {
            @Override
            public void handle(java.sql.ResultSet rs) {

                StringBuilder sb = new StringBuilder();
                while (true) {
                    try {
                        if (!rs.next())
                            break;
                        String orderNumber = rs.getString("ordernumber");
                        String orderDate = rs.getString("orderdate");
                        String status = rs.getString("status");
                        String comments = rs.getString("comments");
                        String customerNumber = rs.getString("customernumber");
                        String customerName = rs.getString("customername");
                        String country = rs.getString("country");

                        sb.append(compactJson("""
                                {"index":{"_id": %s}}
                                        """.formatted(jsonValue(orderNumber)).strip()));
                        sb.append("\n");

                        // Compact JSON representation of the product document
                        sb.append(compactJson("""
                                {
                                    "ordernumber": %s,
                                    "orderdate": %s,
                                    "status": %s,
                                    "comments": %s,
                                    "customernumber": %s,
                                    "customername": %s,
                                    "country": %s
                                }
                                """.formatted(
                                jsonValue(orderNumber),
                                jsonValue(orderDate),
                                jsonValue(status),
                                jsonValue(comments),
                                jsonValue(customerNumber),
                                jsonValue(customerName),
                                jsonValue(country)

                        )));

                        sb.append('\n');
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                connectEs(new EsClientHandler() {
                    @Override
                    public void handle(Rest5Client client) throws SQLException, IOException {
                        String indexName = "customer_orders_view";
                        String customerOrdersViewIndex = """
                                {
                                  "mappings": {
                                    "properties": {
                                      "ordernumber":    { "type": "keyword" },
                                      "orderdate":      { "type": "date" },
                                      "status":         { "type": "keyword" },
                                      "comments":       { "type": "text" },
                                      "customernumber": { "type": "keyword" },
                                      "customername":   { "type": "text" },
                                      "country":        { "type": "keyword" }
                                    }
                                  }
                                }


                                                                                                """;
                        createIndex(client, indexName, customerOrdersViewIndex);
                        bulkInsert(client, indexName, sb.toString());
                    }

                });
            }
        });
    }
}
