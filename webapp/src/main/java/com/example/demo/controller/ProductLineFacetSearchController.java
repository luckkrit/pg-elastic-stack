package com.example.demo.controller;

import java.util.List;
import java.util.Objects;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.demo.elasticsearch.ProductLineFacetSearchService;
import com.example.demo.elasticsearch.ProductPriceRangeFacetOption;
import com.example.demo.elasticsearch.ProductLineFacetOption;
import com.example.demo.elasticsearch.ProductDocument;

@Controller
public class ProductLineFacetSearchController {
    private static final Logger log = LoggerFactory.getLogger(ProductLineFacetSearchController.class);
    private final ProductLineFacetSearchService productLineFacetSearchService;

    public ProductLineFacetSearchController(ProductLineFacetSearchService productLineFacetSearchService) {
        this.productLineFacetSearchService = productLineFacetSearchService;
    }

    @GetMapping("/product-facet-search")
    public String productFacetSearch(@RequestParam(required = false) String selectedProductLine,
            @RequestParam(required = false) Double minPrice, @RequestParam(required = false) Double maxPrice,
            Model model) {
        try {
            SearchResponse<ProductDocument> searchResponse = productLineFacetSearchService.search(selectedProductLine,
                    minPrice, maxPrice);
            log.info("Found {} hits", searchResponse.hits().hits().size());
            log.info("Hits: {}", searchResponse.hits().hits());
            log.info("Aggs: {}", searchResponse.aggregations());
            List<ProductDocument> products = searchResponse.hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
            List<ProductLineFacetOption> productLineFacets = searchResponse.aggregations()
                    .get("by_productline")
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(bucket -> new ProductLineFacetOption(
                            bucket.key().stringValue(),
                            bucket.docCount()))
                    .toList();
            List<ProductPriceRangeFacetOption> productPriceRangeFacets = searchResponse.aggregations()
                    .get("by_price")
                    .range()
                    .buckets()
                    .array()
                    .stream()
                    .map(bucket -> new ProductPriceRangeFacetOption(
                            bucket.key(),
                            bucket.docCount(),
                            bucket.from(),
                            bucket.to()))
                    .toList();

            model.addAttribute(
                    "productLineFacets",
                    productLineFacets);
            model.addAttribute(
                    "productPriceRangeFacets",
                    productPriceRangeFacets);
            model.addAttribute("products", products);
            log.info("selectedProductLine: {}", selectedProductLine==null);
            model.addAttribute("selectedProductLine", selectedProductLine);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during Elasticsearch search", e);
        }
        return "product_facet_search";
    }
}
