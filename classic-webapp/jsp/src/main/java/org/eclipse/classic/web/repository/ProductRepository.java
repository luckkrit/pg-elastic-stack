package org.eclipse.classic.web.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.classic.web.model.Product;
import org.eclipse.classic.web.util.Database;

public class ProductRepository {

    public List<Product> search(String q) {
        List<Product> products = new ArrayList<>();
        String sql = """
                                select p.productcode, p.productname, p.productdescription, p.productline, p.buyprice, p.msrp from classicmodels.products p
                where lower(p.productname) like lower(concat('%', ?, '%'))
                   or lower(p.productdescription) like lower(concat('%', ?, '%'))
                        """;
        try (
                Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {

            ps.setString(1, q);
            ps.setString(2, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                Product product = new Product(
                        rs.getString("productcode"),
                        rs.getString("productname"),
                        rs.getString("productdescription"),
                        rs.getString("productline"),
                        rs.getDouble("buyprice"),
                        rs.getDouble("msrp"));

                products.add(product);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return products;
    }

    public List<Product> findAll() {

        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT p.productcode,p.productname,p.productdescription,p.productline,p.buyprice,p.msrp
                FROM classicmodels.products p
                ORDER BY productname
                """;

        try (
                Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Product product = new Product(
                        rs.getString("productcode"),
                        rs.getString("productname"),
                        rs.getString("productdescription"),
                        rs.getString("productline"),
                        rs.getDouble("buyprice"),
                        rs.getDouble("msrp"));

                products.add(product);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return products;
    }
}