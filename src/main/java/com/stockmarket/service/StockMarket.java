package com.stockmarket.service;

import com.stockmarket.model.*;
import com.stockmarket.exception.*;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockMarket implements IMarketService, IPersistable {
    private List<User> users;
    private List<Stock> stocks;
    private List<Transaction> transactions;
    private PriceUpdater priceUpdater;
    private User currentUser;

    public StockMarket() {
        this.users = new ArrayList<>();
        this.stocks = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.priceUpdater = new PriceUpdater(this);
        this.currentUser = null;
    }

    public void registerUser(String name, String email, String password, double initialBalance) {
        User user = new User(name, email, password, initialBalance);
        users.add(user);
    }

    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                currentUser = user;
                return user;
            }
        }
        return null;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() { return currentUser; }

    public void addStock(Stock stock) {
        stocks.add(stock);
    }

    public Stock findStock(String symbol) {
        return stocks.stream()
            .filter(s -> s.getSymbol().equalsIgnoreCase(symbol))
            .findFirst()
            .orElse(null);
    }

    public List<Stock> getStocks() { return new ArrayList<>(stocks); }
    public List<User> getUsers() { return new ArrayList<>(users); }
    public List<Transaction> getTransactions() { return new ArrayList<>(transactions); }

    @Override
    public void buyStock(User user, Stock stock, int quantity) 
            throws InsufficientBalanceException {
        if (user == null || stock == null || quantity <= 0) {
            throw new IllegalArgumentException("Parâmetros inválidos");
        }

        double totalCost = stock.getCurrentPrice() * quantity;
        user.subtractBalance(totalCost);
        user.addStock(stock.getSymbol(), quantity);
        stock.addVolume(quantity);

        Transaction transaction = new Transaction(
            user.getId(), stock.getSymbol(), TransactionType.BUY, 
            quantity, stock.getCurrentPrice()
        );
        transactions.add(transaction);
    }

    @Override
    public void sellStock(User user, Stock stock, int quantity) 
            throws InsufficientStockException {
        if (user == null || stock == null || quantity <= 0) {
            throw new IllegalArgumentException("Parâmetros inválidos");
        }

        user.removeStock(stock.getSymbol(), quantity);
        double totalValue = stock.getCurrentPrice() * quantity;
        user.addBalance(totalValue);
        stock.addVolume(quantity);

        Transaction transaction = new Transaction(
            user.getId(), stock.getSymbol(), TransactionType.SELL, 
            quantity, stock.getCurrentPrice()
        );
        transactions.add(transaction);
    }

    @Override
    public void updatePrices() {
        Random random = new Random();
        for (Stock stock : stocks) {
            double change = (random.nextDouble() - 0.5) * 0.1;
            double newPrice = stock.getCurrentPrice() * (1 + change);
            stock.updatePrice(newPrice);
        }
    }

    @Override
    public double getStockPrice(Stock stock) {
        return stock.getCurrentPrice();
    }

    public void startPriceUpdates() {
        priceUpdater.start();
    }

    public void stopPriceUpdates() {
        priceUpdater.stop();
    }

    @Override
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Salvar usuários
            writer.println("=== USERS ===");
            for (User user : users) {
                writer.printf("%s;%s;%s;%.2f%n", 
                    user.getName(), user.getEmail(), 
                    user.getPassword(), user.getBalance());
            }

            // Salvar ações
            writer.println("=== STOCKS ===");
            for (Stock stock : stocks) {
                writer.printf("%s;%s;%.2f;%s%n",
                    stock.getName(), stock.getSymbol(),
                    stock.getCurrentPrice(), stock.getSector());
            }

            // Salvar transações
            writer.println("=== TRANSACTIONS ===");
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            for (Transaction t : transactions) {
                writer.printf("%s;%s;%s;%d;%.2f;%s%n",
                    t.getUserId(), t.getStockSymbol(), t.getType(),
                    t.getQuantity(), t.getPrice(),
                    t.getTimestamp().format(formatter));
            }
        }
    }

    @Override
    public void loadFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String section = "";
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("===")) {
                    section = line;
                    continue;
                }

                if (section.equals("=== USERS ===")) {
                    String[] parts = line.split(";");
                    if (parts.length == 4) {
                        registerUser(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]));
                    }
                } else if (section.equals("=== STOCKS ===")) {
                    String[] parts = line.split(";");
                    if (parts.length == 4) {
                        Stock stock = new Stock(parts[0], parts[1], 
                            Double.parseDouble(parts[2]), parts[3]);
                        addStock(stock);
                    }
                }
            }
        }
    }
}