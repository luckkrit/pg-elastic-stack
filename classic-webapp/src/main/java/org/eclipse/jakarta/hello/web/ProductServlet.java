package org.eclipse.jakarta.hello.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jakarta.hello.repository.ProductRepository;

import java.io.IOException;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private final ProductRepository repository =
            new ProductRepository();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute(
                "products",
                repository.findAll()
        );

        request.getRequestDispatcher(
                "/WEB-INF/products.jsp"
        ).forward(request, response);
    }
}