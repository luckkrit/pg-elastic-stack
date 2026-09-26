package com.example.demo.elasticsearch;

public class ProductLineFacetOption {

    private final String name;
    private final long count;

    public ProductLineFacetOption(String name, long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }
}