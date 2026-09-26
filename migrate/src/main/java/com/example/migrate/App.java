package com.example.migrate;

import java.io.IOException;
import java.sql.SQLException;

public class App {

    private static final String PG_URL = System.getenv().getOrDefault(
            "PG_URL", "jdbc:postgresql://127.0.0.1:5432/postgres");
    private static final String PG_USERNAME = System.getenv().getOrDefault("PG_USERNAME", "postgres");
    private static final String PG_PASSWORD = System.getenv().getOrDefault("PG_PASSWORD", "password");
    private static final String ES_URL = System.getenv().getOrDefault("ES_URL", "http://localhost:9200");

    public static void main(String[] args) {
        System.out.println("Starting migration from PostgreSQL to Elasticsearch...");
        try {
            try {
                ProductMapper productMapper = new ProductMapper(PG_URL, PG_USERNAME, PG_PASSWORD, ES_URL);
                productMapper.migrate();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
            try {
                CustomerMapper customerMapper = new CustomerMapper(PG_URL, PG_USERNAME, PG_PASSWORD, ES_URL);
                customerMapper.migrate();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
            try {
                ProductSearchMapper productSearchMapper = new ProductSearchMapper(PG_URL, PG_USERNAME, PG_PASSWORD,
                        ES_URL);
                productSearchMapper.migrate();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
            try {
                CustomerOrdersViewMapper customerOrdersViewMapper = new CustomerOrdersViewMapper(PG_URL, PG_USERNAME,
                        PG_PASSWORD, ES_URL);
                customerOrdersViewMapper.migrate();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } finally

        {
            System.out.println("Migration completed.");
        }

    }
}