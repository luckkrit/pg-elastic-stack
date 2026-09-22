package com.example.demo.elasticsearch;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductSearchService {

    private final ProductSearchRepository repository;

    public ProductSearchService(ProductSearchRepository repository) {
        this.repository = repository;
    }

    public List<ProductDocument> search(String keyword) {
        return repository
                .findByProductNameOrProductDescription(
                        keyword,
                        keyword
                );
    }
}