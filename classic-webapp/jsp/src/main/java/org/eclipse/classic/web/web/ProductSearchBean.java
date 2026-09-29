package org.eclipse.classic.web.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.List;

import org.eclipse.classic.web.model.Product;
import org.eclipse.classic.web.repository.ProductSearchRepository;

@Named
@RequestScoped
public class ProductSearchBean {

    @Inject
    private ProductSearchRepository repository;

    private String q;
    private List<Product> results;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String search() throws IOException {
        results = repository.search(q);
        return null;
    }

    public List<Product> getResults() throws IOException {
        if (results == null) {
            results = repository.search(q);
        }
        return results;
    }
}