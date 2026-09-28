package org.eclipse.jakarta.hello.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import org.eclipse.jakarta.hello.model.Product;
import org.eclipse.jakarta.hello.repository.ProductRepository;

import java.util.List;

@Named
@RequestScoped
public class ProductBean {

    private final ProductRepository repository =
            new ProductRepository();

    public List<Product> getProducts() {
        return repository.findAll();
    }
}