package org.eclipse.classic.web.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.eclipse.classic.web.repository.ProductRepository;
import org.eclipse.classic.web.repository.ProductSearchRepository;

import java.util.logging.Logger;

@WebServlet("/product")
public class ProductServlet extends HttpServlet {
        private static final Logger log = Logger.getLogger(ProductServlet.class.getName());
        private final ProductRepository repository = new ProductRepository();
        private final ProductSearchRepository eSearchRepository = new ProductSearchRepository();

        @Override
        protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws ServletException, IOException {

                String q = request.getParameter("q");
                log.info("q = " + q);
                String es = request.getParameter("es");
                log.info("es = " + es);
                if ((q == null || q == "") && (es == null)) {
                        request.setAttribute(
                                        "products",
                                        repository.findAll());
                        var products = repository.findAll();
                        log.info("fetch database products1 = "+products.size());
                } else {
                        request.setAttribute("q", q);
                        request.setAttribute("es", es!=null);
                        request.setAttribute(
                                        "products",
                                        eSearchRepository.search(q));
                        var products = eSearchRepository.search(q);
                        log.info("fetch elasticsearch products2 = "+products.size());
                }

                request.getRequestDispatcher(
                                "/WEB-INF/views/product.jsp").forward(request, response);
        }

}