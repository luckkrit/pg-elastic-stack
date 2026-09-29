package org.eclipse.classic.web.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import org.eclipse.classic.web.model.Product;
import org.eclipse.classic.web.repository.ProductRepository;
import org.eclipse.classic.web.repository.ProductSearchRepository;

@Named
@RequestScoped
public class ProductBean implements Serializable {

    private static final Logger log =
            Logger.getLogger(ProductBean.class.getName());

    private final ProductRepository repository =
            new ProductRepository();

    private final ProductSearchRepository eSearchRepository =
            new ProductSearchRepository();

    private String q;
    private boolean es;
    private List<Product> products;

    public void load() throws IOException {
        log.info(() -> "q = " + q);
        log.info(() -> "es = " + es);

        if (!es) {
            products = repository.search(q);

            log.info(() ->
                    "fetch database products = " + products.size());
        } else {
            products = eSearchRepository.search(q);

            log.info(() ->
                    "fetch elasticsearch products = " + products.size());
        }
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public boolean isEs() {
        return es;
    }

    public void setEs(boolean es) {
        this.es = es;
    }

    public List<Product> getProducts() {
        return products;
    }
}