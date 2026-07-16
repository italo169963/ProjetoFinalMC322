package com.stockmarket.model;

import java.time.LocalDateTime;

public class Transaction {
    private String userId;
    private String stockSymbol;
    private TransactionType type;
    private int quantity;
    private double price;
    private LocalDateTime timestamp;
    private double totalValue;

    public Transaction(String userId, String stockSymbol, TransactionType type, 
                       int quantity, double price) {
        this.userId = userId;
        this.stockSymbol = stockSymbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
        this.totalValue = quantity * price;
    }

    public String getUserID() { return userId; }
    public String getStockSymbol() { return stockSymbol; }
    public TransactionType getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getTotalValue() { return totalValue; }

    @Override public String toString() {
        return String.format("[%s] %s %d de %s a R$%.2f (Total: R$%.2f)",
            timestamp.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            type == TransactionType.BUY ? "COMPRA" : "VENDA",
            quantity, stockSymbol, price, totalValue);
    }
}