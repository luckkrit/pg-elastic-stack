package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public class ProductLineFacetLink {
    private static String currentPath() {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private static String buildUrl(Map<String, Object> params) {
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(currentPath());
        params.forEach(b::queryParam);
        return b.build().encode().toUriString();
    }

    public String urlProductLinePrice(String selectedProductLine, Double minPrice, Double maxPrice) {
        Map<String, Object> p = new HashMap<>();
        if (selectedProductLine != null) {
            p.put("selectedProductLine", selectedProductLine);
        }
        if (minPrice != null) {
            p.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            p.put("maxPrice", maxPrice);
        }
        return buildUrl(p);
    }
}
