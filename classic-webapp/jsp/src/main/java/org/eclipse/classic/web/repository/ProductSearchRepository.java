package org.eclipse.classic.web.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.StringReader;

import org.eclipse.classic.web.model.Product;
import org.eclipse.classic.web.util.Elasticsearch;

import co.elastic.clients.elasticsearch.core.SearchRequest;

public class ProductSearchRepository {

    // private static final Logger log = Logger.getLogger(ProductSearchRepository.class.getName());

    public List<Product> search(String q) throws IOException {
        String json = (q == null || q.isBlank())
                ? """
                        { "query": { "match_all": {} } }
                        """
                : """
                        {
                                  "query": {
                                    "multi_match": {
                                      "query": %s,
                                      "fields": ["productname", "productdescription"]
                                    }
                                  },
                                  "highlight": {
                                    "pre_tags": ["<mark>"],
                                    "post_tags": ["</mark>"],
                                    "fields": [
                                    {
                                      "productname": {}
                                    },
                                    {
                                      "productdescription": {}
                                    }
                                    ]
                                  }
                                }
                                                """.formatted(Elasticsearch.MAPPER.writeValueAsString(q));

        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index("products");
        builder.withJson(new StringReader(json));

        var res = Elasticsearch.CLIENT.search(
                builder.build(),
                Product.class);

        // log.info("json = "+json);
        // log.info("res = " + res.toString());

        List<Product> products = new ArrayList<>();

        for (var hit : res.hits().hits()) {
            Product p = hit.source();

            if (p == null) {
                continue;
            }

            var hl = hit.highlight();

            if (hl != null) {
                if (hl.containsKey("productname")) {
                    p.setProductName(
                            String.join(" ", hl.get("productname")));
                }

                if (hl.containsKey("productdescription")) {
                    p.setProductDescription(
                            String.join(" ", hl.get("productdescription")));
                }
            }

            products.add(p);
        }

        return products;
    }
}
