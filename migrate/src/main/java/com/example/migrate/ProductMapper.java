package com.example.migrate;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

public class ProductMapper extends DocumentMapper {

    public ProductMapper(String pgUrl, String pgUsername, String pgPassword, String esUrl) {
        super(pgUrl, pgUsername, pgPassword, esUrl);
    }

    @Override
    public void migrate() throws SQLException, IOException {
        processQuery("SELECT * FROM classicmodels.products", new ResultSetHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException, IOException {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    String productcode = rs.getString("productcode");
                    String productname = rs.getString("productname");
                    String productline = rs.getString("productline");
                    String productdescription = rs.getString("productdescription");
                    String productscale = rs.getString("productscale");
                    String productvendor = rs.getString("productvendor");
                    int quantityinstock = rs.getInt("quantityinstock");
                    float buyprice = rs.getFloat("buyprice");
                    float msrp = rs.getFloat("msrp");
                    sb.append(compactJson("""
                            {"index":{"_id": %s}}
                                    """.formatted(jsonValue(productcode)).strip()));
                    sb.append("\n");
                    // Compact JSON representation of the product document
                    sb.append(compactJson("""
                            {
                                "productcode": %s,
                                "productname": %s,
                                "productline": %s,
                                "productdescription": %s,
                                "productscale": %s,
                                "productvendor": %s,
                                "quantityinstock": %s,
                                "buyprice": %s,
                                "msrp": %s
                            }
                            """.formatted(
                            jsonValue(productcode),
                            jsonValue(productname),
                            jsonValue(productline),
                            jsonValue(productdescription),
                            jsonValue(productscale),
                            jsonValue(productvendor),
                            jsonValue(quantityinstock),
                            jsonValue(buyprice),
                            jsonValue(msrp))));

                    sb.append('\n');
                }
                sb.append("\n");
                connectEs(new EsClientHandler() {
                    @Override
                    public void handle(Rest5Client client) throws SQLException, IOException {
                        String indexName = "products";
                        String productIndex = """
                                {
                                    "mappings": {
                                        "properties": {
                                            "productcode": { "type": "keyword" },
                                            "productname": { "type": "text" },
                                            "productline": { "type": "keyword" },
                                            "productdescription": { "type": "text" },
                                            "productscale":       { "type": "keyword" },
                                            "productvendor":      { "type": "keyword" },
                                            "buyprice": { "type": "float" },
                                            "msrp": { "type": "float" },
                                            "quantityinstock":    { "type": "integer" }
                                        }
                                    }
                                }
                                """;
                        createIndex(client, indexName, productIndex);
                        bulkInsert(client, indexName, sb.toString());
                    }
                });
            }

        });
    }

}
