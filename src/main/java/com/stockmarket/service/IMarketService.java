package com.stockmarket.service;

import com.stockmarket.model.*;

public interface IMarketService {
    void buyStock(User user, Stock stock, int quantity)
        throws InsufficientBalanceException;
    void sellStock(User user, Stock stock, int quantity)
        throws InsufficientStockException;
    void uptadePrices();
    double getStockPrice(Stock stock);
}