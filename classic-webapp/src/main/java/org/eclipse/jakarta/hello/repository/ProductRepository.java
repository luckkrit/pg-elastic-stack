package org.eclipse.jakarta.hello.repository;

import org.eclipse.jakarta.hello.model.Product;
import org.eclipse.jakarta.hello.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public List<Product> findAll() {

        List<Product> products = new ArrayList<>();

        String sql = """
            SELECT productcode,
                   productname,
                   productline,
                   buyprice,
                   msrp
            FROM classicmodels.products
            ORDER BY productname
            """;

        try (
            Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Product product = new Product(
                        rs.getString("productcode"),
                        rs.getString("productname"),
                        rs.getString("productline"),
                        rs.getDouble("buyprice"),
                        3.0
                );

                products.add(product);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return products;
    }
}