package com.emanuelvictor.erp.domain.stock.domain.model;

import java.util.UUID;

public class Product {

    private UUID id;
    private String name;

    public Product(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }
}
