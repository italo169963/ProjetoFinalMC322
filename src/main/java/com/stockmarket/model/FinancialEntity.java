package com.stockmarket.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class FinancialEntity {
    protected String id;
    protected String name;
    protected LocalDateTime createdAt;

    public FinancialEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setName(String name) { this.name = name; }
}