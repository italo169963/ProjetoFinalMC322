package com.stockmarket.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String transactionId;
    private String userId;
    private String stockSymbol;
    private TransactionType type;
    private int quantity;
    private double price;
    private LocalDateTime timestamp;
    private double totalValue;
    private String userName;

    public Transaction(String userId, String stockSymbol, TransactionType type, 
                       int quantity, double price) {
        this.transactionId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.stockSymbol = stockSymbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
        this.totalValue = quantity * price;
        this.userName = null;
    }

    public Transaction(String userId, String userName, String stockSymbol, 
                       TransactionType type, int quantity, double price) {
        this(userId, stockSymbol, type, quantity, price);
        this.userName = userName;
    }

    public String getTransactionId() { return transactionId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getStockSymbol() { return stockSymbol; }
    public TransactionType getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getTotalValue() { return totalValue; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getTypeString() {
        return type == TransactionType.BUY ? "COMPRA" : "VENDA";
    }

    @Override
    public String toString() {
        String userInfo = userName != null ? userName + " (" + userId + ")" : userId;
        return String.format("[%s] %s %s %d de %s a R$%.2f (Total: R$%.2f)", 
            getFormattedTimestamp(),
            userInfo,
            getTypeString(),
            quantity, 
            stockSymbol, 
            price, 
            totalValue
        );
    }

    public String toShortString() {
        return String.format("%s: %d %s @ R$%.2f = R$%.2f", 
            getTypeString(), quantity, stockSymbol, price, totalValue);
    }
}