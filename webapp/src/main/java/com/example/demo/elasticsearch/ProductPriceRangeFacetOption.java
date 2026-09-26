package com.example.demo.elasticsearch;

public class ProductPriceRangeFacetOption {

    private final String name;
    private final long count;
    private final Double from;
    private final Double to;

    public ProductPriceRangeFacetOption(String name, long count, Double from, Double to) {
        this.name = name;
        this.count = count;
        this.from = from;
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    public Double getFrom() {
        return from;
    }

    public Double getTo() {
        return to;
    }
}
