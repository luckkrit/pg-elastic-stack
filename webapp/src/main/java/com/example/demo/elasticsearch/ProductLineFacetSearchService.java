package com.example.demo.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.controller.ProductLineFacetSearchController;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import java.io.StringReader;
import java.io.IOException;

@Service
public class ProductLineFacetSearchService {
  private static final Logger log = LoggerFactory.getLogger(ProductLineFacetSearchController.class);
  private final ElasticsearchClient esClient;

  public ProductLineFacetSearchService(ElasticsearchClient esClient) {
    this.esClient = esClient;
  }

  public SearchResponse<ProductDocument> search(String q, String selectedProductLine, Double minPrice, Double maxPrice)
      throws IOException {

    String productLineFilter = (selectedProductLine == null) ? "" : String.format("""
        {
          "term": {
            "productline": "%s"
          }
        }
        """, selectedProductLine);
    String pricePart = "";
    if (minPrice != null) {
      pricePart += """
          "gte": %f
          """.formatted(minPrice);
    }
    if (maxPrice != null) {
      if (!pricePart.isEmpty())
        pricePart += ",";
      pricePart += """
          "lt": %f
          """.formatted(maxPrice);
    }
    log.info("price part = " + pricePart);
    String priceRangePart = !productLineFilter.isEmpty() ? """
                           ,{
                              "range": {
                                "buyprice": {
                                 %s
                                }
                              }
                            }

        """.formatted(pricePart) : """

                       {
                          "range": {
                            "buyprice": {
                             %s
                            }
                          }
                        }
        """.formatted(pricePart);
    log.info("price range part = " + priceRangePart);
    String postFilterJson = """
          "post_filter": {
            "bool": {
              "filter": [
                %s
                %s
              ]
            }
          }
        """;
    String postFilterPart = postFilterJson.formatted(productLineFilter, priceRangePart);

    log.info("postFilterPart =  " + postFilterPart);
    String matchPart = q == null || q.isEmpty() ? """
        "match_all": {}
        """ : """
        "multi_match": { "query": "%s", "fields": ["productname","productdescription"] }
        """.formatted(q);
    log.info("matchPart = {}", matchPart);
    String rawJson = """
                {
                  "size": 40,
                  "query": {
                    %s
                  },
          "highlight": {
          "pre_tags": ["<mark class='badge badge-neutral'>"],
          "post_tags": ["</mark>"],
          "fields": [{
            "productname": {}
          },{"productdescription":{}}]
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
                                """.formatted(matchPart, postFilterPart.isEmpty() ? "" : "," + postFilterPart);
    log.info("Raw JSON query: {}", rawJson);
    log.info("selectedProductLine: {}", selectedProductLine);
    SearchRequest request = SearchRequest.of(b -> b
        .index("products")
        .withJson(new StringReader(rawJson)));

    return esClient.search(request, ProductDocument.class);
  }

}
