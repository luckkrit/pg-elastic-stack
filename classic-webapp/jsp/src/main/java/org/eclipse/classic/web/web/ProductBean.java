package org.eclipse.classic.web.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.util.List;

import org.eclipse.classic.web.model.Product;
import org.eclipse.classic.web.repository.ProductRepository;

@Named
@RequestScoped
public class ProductBean {

    private final ProductRepository repository =
            new ProductRepository();

    public List<Product> getProducts() {
        return repository.findAll();
    }
}