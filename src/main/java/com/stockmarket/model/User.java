package com.stockmarket.model;

import java.util.HashMap;
import java.util.Map;

public class User extends FinancialEntity {
    private String email;
    private String password;
    private double balance;
    private Map<String, Integer> portfolio;

    public User(String name, String email, String password, double initialBalance) {
        super(name);
        this.email = email;
        this.password = password;
        this.balance = initialBalance;
        this.portfolio = new HashMap<>();
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public double getBalance() { return balance; }
    public Map<String, Integer> getPortfolio() { return portfolio; }

    public void addBalance(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public void subtractBalance(double amount) throws InsufficientBalanceException {
        if (this.balance < amount) {
            throw new InsufficientBalanceException("Saldo insuficiente. Disponível: R$" + 
                String.format("%.2f", this.balance) + ", Necessário: R$" + 
                String.format("%.2f", amount));
        }
        this.balance -= amount;
    }

    public void addStock(String symbol, int quantity) {
        portfolio.put(symbol, portfolio.getOrDefault(symbol, 0) + quantity);
    }

    public void removeStock(String symbol, int quantity) throws InsufficientStockException {
        int currentQty = portfolio.getOrDefault(symbol, 0);
        if (currentQty < quantity) {
            throw new InsufficientStockException("Quantidade insuficiente de " + symbol + 
                ". Disponível: " + currentQty + ", Solicitado: " + quantity);
        }
        if (currentQty == quantity) {
            portfolio.remove(symbol);
        } else {
            portfolio.put(symbol, currentQty - quantity);
        }
    }

    public double getTotalPortfolioValue(Map<String, Double> currentPrices) {
        double total = 0;
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            Double price = currentPrices.get(entry.getKey());
            if (price != null) {
                total += price * entry.getValue();
            }
        }
        return total;
    }
}