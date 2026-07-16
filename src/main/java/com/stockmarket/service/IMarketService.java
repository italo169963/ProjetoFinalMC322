package com.stockmarket.service;

import com.stockmarket.model.*;
import com.stockmarket.exception.*;
import java.util.List;

public interface IMarketService {
    /**
     * Compra uma quantidade de ações para um usuário
     */
    void buyStock(User user, Stock stock, int quantity) 
        throws InsufficientBalanceException, IllegalArgumentException;

    /**
     * Vende uma quantidade de ações de um usuário
     */
    void sellStock(User user, Stock stock, int quantity) 
        throws InsufficientStockException, IllegalArgumentException;

    /**
     * Atualiza os preços de todas as ações
     */
    void updatePrices();

    /**
     * Obtém o preço atual de uma ação
     */
    double getStockPrice(Stock stock);

    /**
     * Obtém o preço atual de uma ação pelo símbolo
     */
    double getStockPrice(String symbol);

    /**
     * Registra um novo usuário no sistema
     */
    void registerUser(String name, String email, String password, double initialBalance);

    /**
     * Autentica um usuário no sistema
     */
    User login(String email, String password);

    /**
     * Adiciona uma nova ação ao mercado
     */
    void addStock(Stock stock);

    /**
     * Busca uma ação pelo símbolo
     */
    Stock findStock(String symbol);

    /**
     * Obtém todas as transações de um usuário
     */
    List<Transaction> getUserTransactions(User user);

    /**
     * Calcula o valor total da carteira de um usuário
     */
    double calculatePortfolioValue(User user);
}