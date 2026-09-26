package com.example.demo.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.controller.ProductLineFacetSearchController;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.io.StringReader;
import java.io.IOException;
import java.util.List;
@Service
public class ProductLineFacetSearchService {
      private static final Logger log = LoggerFactory.getLogger(ProductLineFacetSearchController.class);
    private final ElasticsearchClient esClient;

    public ProductLineFacetSearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public SearchResponse<ProductDocument> search(String selectedProductLine) throws IOException {
        String postFilterJson = """
        "post_filter":{"term":{"productline":"%s"}}
        """;
        String postFilterPart = (selectedProductLine == null)
                ? ""
                : String.format(postFilterJson, selectedProductLine);

        String rawJson = """
                {
                  "query": { "match_all": {} },
                  "aggs": {
                    "by_productline": { "terms": { "field": "productline" } }
                  }
                  %s
                }
                """.formatted(postFilterPart.isEmpty() ? "" : "," + postFilterPart);

        log.info("Raw JSON query: {}", rawJson);
        log.info("selectedProductLine: {}", selectedProductLine);
        SearchRequest request = SearchRequest.of(b -> b
                .index("products")
                .withJson(new StringReader(rawJson)));

        return esClient.search(request, ProductDocument.class);
    }

}
