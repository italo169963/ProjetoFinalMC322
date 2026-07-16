package com.stockmarket.service;

import com.stockmarket.model.*;
import com.stockmarket.exception.*;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class StockMarket implements IMarketService, IPersistable {
    private List<User> users;
    private List<Stock> stocks;
    private List<Transaction> transactions;
    private PriceUpdater priceUpdater;
    private User currentUser;
    private Map<String, Double> lastPrices;

    public StockMarket() {
        this.users = new ArrayList<>();
        this.stocks = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.priceUpdater = new PriceUpdater(this);
        this.currentUser = null;
        this.lastPrices = new HashMap<>();
    }

    // ========== Implementação IMarketService ==========

    @Override
    public void buyStock(User user, Stock stock, int quantity) 
            throws InsufficientBalanceException {
        if (user == null || stock == null) {
            throw new IllegalArgumentException("Usuário e ação não podem ser nulos");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        double totalCost = stock.getCurrentPrice() * quantity;
        user.subtractBalance(totalCost);
        user.addStock(stock.getSymbol(), quantity);
        stock.addVolume(quantity);
        lastPrices.put(stock.getSymbol(), stock.getCurrentPrice());

        Transaction transaction = new Transaction(
            user.getId(), user.getName(), stock.getSymbol(), 
            TransactionType.BUY, quantity, stock.getCurrentPrice()
        );
        transactions.add(transaction);
    }

    @Override
    public void sellStock(User user, Stock stock, int quantity) 
            throws InsufficientStockException {
        if (user == null || stock == null) {
            throw new IllegalArgumentException("Usuário e ação não podem ser nulos");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        user.removeStock(stock.getSymbol(), quantity);
        double totalValue = stock.getCurrentPrice() * quantity;
        user.addBalance(totalValue);
        stock.addVolume(quantity);
        lastPrices.put(stock.getSymbol(), stock.getCurrentPrice());

        Transaction transaction = new Transaction(
            user.getId(), user.getName(), stock.getSymbol(), 
            TransactionType.SELL, quantity, stock.getCurrentPrice()
        );
        transactions.add(transaction);
    }

    @Override
    public void updatePrices() {
        Random random = new Random();
        for (Stock stock : stocks) {
            double change = (random.nextDouble() - 0.5) * 0.08; // Variação de até ±4%
            double newPrice = stock.getCurrentPrice() * (1 + change);
            // Garantir que o preço não fique negativo
            newPrice = Math.max(newPrice, 0.01);
            stock.updatePrice(newPrice);
            lastPrices.put(stock.getSymbol(), newPrice);
        }
    }

    @Override
    public double getStockPrice(Stock stock) {
        if (stock == null) return 0;
        return stock.getCurrentPrice();
    }

    @Override
    public double getStockPrice(String symbol) {
        Stock stock = findStock(symbol);
        return stock != null ? stock.getCurrentPrice() : 0;
    }

    @Override
    public void registerUser(String name, String email, String password, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Saldo inicial não pode ser negativo");
        }
        // Verificar se email já existe
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException("Email já cadastrado");
            }
        }
        User user = new User(name, email, password, initialBalance);
        users.add(user);
    }

    @Override
    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && 
                user.getPassword().equals(password)) {
                currentUser = user;
                return user;
            }
        }
        return null;
    }

    @Override
    public void addStock(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Ação não pode ser nula");
        }
        // Verificar se símbolo já existe
        if (findStock(stock.getSymbol()) != null) {
            throw new IllegalArgumentException("Símbolo já cadastrado: " + stock.getSymbol());
        }
        stocks.add(stock);
        lastPrices.put(stock.getSymbol(), stock.getCurrentPrice());
    }

    @Override
    public Stock findStock(String symbol) {
        if (symbol == null) return null;
        return stocks.stream()
            .filter(s -> s.getSymbol().equalsIgnoreCase(symbol.trim()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<Transaction> getUserTransactions(User user) {
        if (user == null) return new ArrayList<>();
        return transactions.stream()
            .filter(t -> t.getUserId().equals(user.getId()))
            .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
            .collect(Collectors.toList());
    }

    @Override
    public double calculatePortfolioValue(User user) {
        if (user == null) return 0;
        Map<String, Double> prices = new HashMap<>();
        for (Stock stock : stocks) {
            prices.put(stock.getSymbol(), stock.getCurrentPrice());
        }
        return user.getTotalPortfolioValue(prices);
    }

    // ========== Métodos adicionais ==========

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() { return currentUser; }

    public List<Stock> getStocks() { return new ArrayList<>(stocks); }

    public List<User> getUsers() { return new ArrayList<>(users); }

    public List<Transaction> getTransactions() { 
        return new ArrayList<>(transactions); 
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return transactions.stream()
            .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public Map<String, Double> getLastPrices() {
        return new HashMap<>(lastPrices);
    }

    public void startPriceUpdates() {
        if (priceUpdater != null) {
            priceUpdater.start();
        }
    }

    public void stopPriceUpdates() {
        if (priceUpdater != null) {
            priceUpdater.stop();
        }
    }

    public boolean isPriceUpdaterRunning() {
        return priceUpdater != null && priceUpdater.isRunning();
    }

    public Stock getMostActiveStock() {
        return stocks.stream()
            .max(Comparator.comparingInt(Stock::getVolume))
            .orElse(null);
    }

    public Stock getBestPerformer() {
        return stocks.stream()
            .max(Comparator.comparingDouble(Stock::getDailyChange))
            .orElse(null);
    }

    public Stock getWorstPerformer() {
        return stocks.stream()
            .min(Comparator.comparingDouble(Stock::getDailyChange))
            .orElse(null);
    }

    public int getTotalTransactions() {
        return transactions.size();
    }

    public double getTotalVolume() {
        return stocks.stream().mapToDouble(Stock::getVolume).sum();
    }

    // ========== Implementação IPersistable ==========

    @Override
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Salvar usuários
            writer.println("=== USERS ===");
            writer.println("name;email;password;balance");
            for (User user : users) {
                writer.printf("%s;%s;%s;%.2f%n", 
                    user.getName(), user.getEmail(), 
                    user.getPassword(), user.getBalance());
            }

            // Salvar ações
            writer.println("=== STOCKS ===");
            writer.println("name;symbol;price;sector;volume");
            for (Stock stock : stocks) {
                writer.printf("%s;%s;%.2f;%s;%d%n",
                    stock.getName(), stock.getSymbol(),
                    stock.getCurrentPrice(), stock.getSector(), 
                    stock.getVolume());
            }

            // Salvar transações
            writer.println("=== TRANSACTIONS ===");
            writer.println("userId;userName;symbol;type;quantity;price;timestamp");
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            for (Transaction t : transactions) {
                writer.printf("%s;%s;%s;%s;%d;%.2f;%s%n",
                    t.getUserId(), 
                    t.getUserName() != null ? t.getUserName() : "",
                    t.getStockSymbol(), 
                    t.getType().name(),
                    t.getQuantity(), 
                    t.getPrice(),
                    t.getTimestamp().format(formatter));
            }
        }
    }

    @Override
    public void loadFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String section = "";
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("===")) {
                    section = line;
                    continue;
                }

                if (section.equals("=== USERS ===") && !line.startsWith("name;")) {
                    String[] parts = line.split(";");
                    if (parts.length >= 4) {
                        try {
                            registerUser(parts[0], parts[1], parts[2], 
                                Double.parseDouble(parts[3]));
                        } catch (Exception e) {
                            System.err.println("Erro ao carregar usuário: " + e.getMessage());
                        }
                    }
                } else if (section.equals("=== STOCKS ===") && !line.startsWith("name;")) {
                    String[] parts = line.split(";");
                    if (parts.length >= 5) {
                        try {
                            Stock stock = new Stock(parts[0], parts[1], 
                                Double.parseDouble(parts[2]), parts[3]);
                            stock.addVolume(Integer.parseInt(parts[4]));
                            addStock(stock);
                        } catch (Exception e) {
                            System.err.println("Erro ao carregar ação: " + e.getMessage());
                        }
                    }
                } else if (section.equals("=== TRANSACTIONS ===") && !line.startsWith("userId;")) {
                    String[] parts = line.split(";");
                    if (parts.length >= 7) {
                        try {
                            Transaction t = new Transaction(
                                parts[0],
                                parts[1],
                                parts[2],
                                TransactionType.valueOf(parts[3]),
                                Integer.parseInt(parts[4]),
                                Double.parseDouble(parts[5])
                            );
                            // Note: timestamp não é definido, usamos o atual
                            transactions.add(t);
                        } catch (Exception e) {
                            System.err.println("Erro ao carregar transação: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void exportToCSV(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Exportar ações
            writer.println("Symbol,Name,Price,Change,Volume,Sector");
            for (Stock stock : stocks) {
                writer.printf("%s,%s,%.2f,%.2f%%,%d,%s%n",
                    stock.getSymbol(), stock.getName(),
                    stock.getCurrentPrice(), stock.getPriceChange(),
                    stock.getVolume(), stock.getSector());
            }

            writer.println();
            writer.println("User,Email,Balance,Stocks");
            for (User user : users) {
                writer.printf("%s,%s,%.2f,%d%n",
                    user.getName(), user.getEmail(),
                    user.getBalance(), user.getPortfolio().size());
            }
        }
    }

    @Override
    public void importFromCSV(String filename) throws IOException {
        // Implementação simplificada - poderia ser expandida
        throw new UnsupportedOperationException("Importação CSV não implementada");
    }

    @Override
    public boolean fileExists(String filename) {
        return new File(filename).exists();
    }
}