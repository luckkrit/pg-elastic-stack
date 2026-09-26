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

        public SearchResponse<ProductDocument> search(String selectedProductLine, Double minPrice, Double maxPrice)
                        throws IOException {
                
                
                String productLineFilter = (selectedProductLine == null) ? "" : String.format("""
                                {
                                  "term": {
                                    "productline": "%s"
                                  }
                                },
                                """, selectedProductLine);
                String postFilterJson = """
                                  "post_filter": {
                                    "bool": {
                                      "filter": [
                                        %s
                                        {
                                          "range": {
                                            "buyprice": {
                                              "gte": %f,
                                              "lt": %f
                                            }
                                          }
                                        }
                                      ]
                                    }
                                  }
                                """;
                String postFilterPart = (minPrice == null && maxPrice == null)
                                ? ""
                                : String.format(postFilterJson, productLineFilter, minPrice, maxPrice);

                log.info("postFilterPart: {}", postFilterPart);
                String rawJson = """
                                {
                                  "size": 40,
                                  "query": {
                                    "match_all": {}
                                  },
                                  "aggs": {
                                    "by_productline": {
                                      "terms": {
                                        "field": "productline"
                                      }
                                    },
                                    "by_price": {
                                      "range": {
                                        "field": "buyprice",
                                        "ranges": [
                                          {
                                            "key": "Under $50",
                                            "to": 50
                                          },
                                          {
                                            "key": "$50 - $99.99",
                                            "from": 50,
                                            "to": 100
                                          },
                                          {
                                            "key": "$100 - $199.99",
                                            "from": 100,
                                            "to": 200
                                          },
                                          {
                                            "key": "$200 and above",
                                            "from": 200
                                          }
                                        ]
                                      }
                                    }
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
