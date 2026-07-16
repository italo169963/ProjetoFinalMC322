package com.stockmarket.model;

import java.util.ArrayList;
import java.util.List;

public class Stock extends FinancialEntity {
    private String symbol;
    private double currentPrice;
    private int volume;
    private String sector;
    private List<Double> priceHistory;

    public Stock(String name, String symbol, double initialPrice, String sector) {
        super(name);
        this.symbol = symbol;
        this.currentPrice = initialPrice;
        this.sector = sector;
        this.volume = 0;
        this.priceHistory = new ArrayList<>();
        this.priceHistory.add(initialPrice);
    }

    public String getSymbol() { return symbol; }
    public double getCurrentPrice() { return currentPrice; }
    public int getVolume() { return volume; }
    public String getSector() { return sector; }
    public List<Double> getPriceHistory() { return new ArrayList<>(priceHistory); }
    public double getOpeningPrice() { return openingPrice; }

    public void setSector(String sector) {this.sector = sector; }

    public void updatePrice(double newPrice) {
        if (newPrice > 0) {
            this.currentPrice = newPrice;
            this.priceHistory.add(newPrice);
            if (priceHistory.size() > 100) {
                priceHistory.remove(0);
            }
        }
    }

    public void addVolume(int quantity) {
        this.volume += quantity;
    }

    public double getPriceChange() {
        if (priceHistory.size() < 2) return 0;
        double previous = priceHistory.get(priceHistory.size() - 2);
        return ((currentPrice - previous) / previous) * 100;
    }

     public double getDailyChange() {
        if (openingPrice == 0) return 0;
        return ((currentPrice - openingPrice) / openingPrice) * 100;
    }

    public double getMaxPrice() {
        return priceHistory.stream().max(Double::compareTo).orElse(currentPrice);
    }

    public double getMinPrice() {
        return priceHistory.stream().min(Double::compareTo).orElse(currentPrice);
    }

    public double getAveragePrice() {
        return priceHistory.stream().mapToDouble(Double::doubleValue).average().orElse(currentPrice);
    }

    @Override
    public String toString() {
        return String.format("Stock{symbol='%s', price=%.2f, sector='%s', volume=%d}", 
            symbol, currentPrice, sector, volume);
    }
}