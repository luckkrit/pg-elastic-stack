package com.example.migrate;

import java.io.IOException;
import java.sql.SQLException;

import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

public class CustomerMapper extends DocumentMapper {

    protected CustomerMapper(String pgUrl, String pgUsername, String pgPassword, String esUrl) {
        super(pgUrl, pgUsername, pgPassword, esUrl);
    }

    @Override
    public void migrate() throws SQLException, IOException {
        processQuery("SELECT * FROM classicmodels.customers", new ResultSetHandler() {
            @Override
            public void handle(java.sql.ResultSet rs) throws SQLException, IOException {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    String customerNumber = rs.getString("customernumber");
                    String customerName = rs.getString("customername");
                    String contactLastName = rs.getString("contactlastname");
                    String contactFirstName = rs.getString("contactfirstname");
                    String phone = rs.getString("phone");
                    String addressLine1 = rs.getString("addressline1");
                    String addressLine2 = rs.getString("addressline2");
                    String city = rs.getString("city");
                    String state = rs.getString("state");
                    String postalCode = rs.getString("postalcode");
                    String country = rs.getString("country");
                    int salesRepEmployeeNumber = rs.getInt("salesrepemployeenumber");
                    float creditLimit = rs.getFloat("creditlimit");
                    String customerLocation = rs.getString("customerlocation");

                    sb.append(compactJson("""
                            {"index":{"_id": %s}}
                                    """.formatted(jsonValue(customerNumber)).strip()));
                    sb.append("\n");

                    // Compact JSON representation of the customer document
                    sb.append(compactJson("""
                            {
                                "customerNumber": %s,
                                "customerName": %s,
                                "contactLastName": %s,
                                "contactFirstName": %s,
                                "phone": %s,
                                "addressLine1": %s,
                                "addressLine2": %s,
                                "city": %s,
                                "state": %s,
                                "postalCode": %s,
                                "country": %s,
                                "salesRepEmployeeNumber": %s,
                                "creditLimit": %s
                            }
                            """.formatted(
                            jsonValue(customerNumber),
                            jsonValue(customerName),
                            jsonValue(contactLastName),
                            jsonValue(contactFirstName),
                            jsonValue(phone),
                            jsonValue(addressLine1),
                            jsonValue(addressLine2),
                            jsonValue(city),
                            jsonValue(state),
                            jsonValue(postalCode),
                            jsonValue(country),
                            jsonValue(salesRepEmployeeNumber),
                            jsonValue(creditLimit),
                            jsonValue(customerLocation))));

                    sb.append('\n');
                }
                sb.append("\n");
                connectEs(new EsClientHandler() {
                    @Override
                    public void handle(Rest5Client client) throws SQLException, IOException {
                        String indexName = "customers";
                        String customerIndex = """
                                {
                                  "mappings": {
                                    "properties": {
                                      "customernumber":         { "type": "keyword" },
                                      "customername":           { "type": "text" },
                                      "contactlastname":        { "type": "text" },
                                      "contactfirstname":       { "type": "text" },
                                      "phone":                  { "type": "keyword" },
                                      "addressline1":           { "type": "text" },
                                      "addressline2":           { "type": "text" },
                                      "city":                   { "type": "text" },
                                      "state":                  { "type": "keyword" },
                                      "postalcode":             { "type": "keyword" },
                                      "country":                { "type": "keyword" },
                                      "salesrepemployeenumber": { "type": "keyword" },
                                      "creditlimit":            { "type": "float" },
                                      "customerlocation":       { "type": "keyword" }
                                    }
                                  }
                                }

                                                                """;
                        createIndex(client, indexName, customerIndex);
                        bulkInsert(client, indexName, sb.toString());
                    }

                });
            }
        });
    }

}
