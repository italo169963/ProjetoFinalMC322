package com.stockmarket.model;

public enum TransactionType {
    BUY("Compra"),
    SELL("Venda");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBuy() {
        return this == BUY;
    }

    public boolean isSell() {
        return this == SELL;
    }

    @Override
    public String toString() {
        return description;
    }
}