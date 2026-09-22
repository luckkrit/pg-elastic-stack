package com.example.demo.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument>
    findByProductnameOrProductdescription(
            String productName,
            String productDescription
    );
}