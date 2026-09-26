package com.example.demo.elasticsearch;

public class FacetOption {

    private final String name;
    private final long count;

    public FacetOption(String name, long count) {
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