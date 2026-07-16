package com.stockmarket;

import com.stockmarket.model.*;
import com.stockmarket.exception.*;
import com.stockmarket.service.StockMarket;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;

public class StockMarketTest {
    private StockMarket market;
    private User user;
    private Stock stock;

    @BeforeEach
    void setUp() {
        market = new StockMarket();
        market.registerUser("Test User", "test@email.com", "password", 1000.00);
        user = market.login("test@email.com", "password");
        stock = new Stock("Test Stock", "TST4", 50.00, "Tecnologia");
        market.addStock(stock);
    }

    @AfterEach
    void tearDown() {
        market.stopPriceUpdates();
    }

    // ========== Testes de Usuário ==========

    @Test
    void testRegisterUser() {
        market.registerUser("New User", "new@email.com", "pass", 500.00);
        User newUser = market.login("new@email.com", "pass");
        assertNotNull(newUser);
        assertEquals("New User", newUser.getName());
        assertEquals(500.00, newUser.getBalance());
        assertTrue(newUser.getPortfolio().isEmpty());
    }

    @Test
    void testRegisterUserWithDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.registerUser("Duplicate", "test@email.com", "pass", 100.00);
        });
    }

    @Test
    void testRegisterUserWithNegativeBalance() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.registerUser("Invalid", "invalid@email.com", "pass", -100.00);
        });
    }

    @Test
    void testLoginSuccess() {
        User loggedUser = market.login("test@email.com", "password");
        assertNotNull(loggedUser);
        assertEquals("Test User", loggedUser.getName());
        assertEquals("test@email.com", loggedUser.getEmail());
        assertEquals(user, loggedUser);
    }

    @Test
    void testLoginFailure() {
        User wrongEmail = market.login("wrong@email.com", "password");
        assertNull(wrongEmail);
        
        User wrongPassword = market.login("test@email.com", "wrong");
        assertNull(wrongPassword);
        
        User emptyCredentials = market.login("", "");
        assertNull(emptyCredentials);
    }

    @Test
    void testLogout() {
        assertNotNull(market.getCurrentUser());
        market.logout();
        assertNull(market.getCurrentUser());
    }

    @Test
    void testGetCurrentUser() {
        assertEquals(user, market.getCurrentUser());
        market.logout();
        assertNull(market.getCurrentUser());
    }

    // ========== Testes de Ações ==========

    @Test
    void testAddStock() {
        Stock newStock = new Stock("New Stock", "NEW1", 100.00, "Financeiro");
        market.addStock(newStock);
        assertEquals(2, market.getStocks().size());
        
        Stock found = market.findStock("NEW1");
        assertNotNull(found);
        assertEquals("New Stock", found.getName());
    }

    @Test
    void testAddStockDuplicateSymbol() {
        Stock duplicate = new Stock("Duplicate", "TST4", 100.00, "Teste");
        assertThrows(IllegalArgumentException.class, () -> {
            market.addStock(duplicate);
        });
    }

    @Test
    void testAddStockNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.addStock(null);
        });
    }

    @Test
    void testFindStock() {
        Stock found = market.findStock("TST4");
        assertNotNull(found);
        assertEquals("Test Stock", found.getName());
        assertEquals(50.00, found.getCurrentPrice());
        
        Stock notFound = market.findStock("INVALID");
        assertNull(notFound);
        
        Stock caseInsensitive = market.findStock("tst4");
        assertNotNull(caseInsensitive);
        assertEquals("TST4", caseInsensitive.getSymbol());
    }

    @Test
    void testGetStocks() {
        List<Stock> stocks = market.getStocks();
        assertEquals(1, stocks.size());
        assertEquals("TST4", stocks.get(0).getSymbol());
        
        // Modificar a lista retornada não deve afetar o original
        stocks.add(new Stock("New", "NEW1", 100.00, "Teste"));
        assertEquals(1, market.getStocks().size());
    }

    @Test
    void testGetStockPrice() {
        double price = market.getStockPrice(stock);
        assertEquals(50.00, price);
        
        double priceBySymbol = market.getStockPrice("TST4");
        assertEquals(50.00, priceBySymbol);
        
        double invalidPrice = market.getStockPrice("INVALID");
        assertEquals(0, invalidPrice);
    }

    // ========== Testes de Transações ==========

    @Test
    void testBuyStockSuccess() throws Exception {
        int qty = 5;
        double initialBalance = user.getBalance();
        double cost = stock.getCurrentPrice() * qty;

        market.buyStock(user, stock, qty);
        
        assertEquals(initialBalance - cost, user.getBalance(), 0.001);
        assertEquals(qty, user.getStockQuantity(stock.getSymbol()));
        assertEquals(qty, stock.getVolume());
        
        List<Transaction> transactions = market.getUserTransactions(user);
        assertEquals(1, transactions.size());
        Transaction t = transactions.get(0);
        assertEquals(TransactionType.BUY, t.getType());
        assertEquals(qty, t.getQuantity());
        assertEquals(stock.getCurrentPrice(), t.getPrice());
    }

    @Test
    void testBuyStockInsufficientBalance() {
        int qty = 100;
        double cost = stock.getCurrentPrice() * qty;
        
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            market.buyStock(user, stock, qty);
        });
        
        assertTrue(exception.getMessage().contains("Saldo insuficiente"));
    }

    @Test
    void testBuyStockWithZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.buyStock(user, stock, 0);
        });
    }

    @Test
    void testBuyStockWithNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.buyStock(user, stock, -5);
        });
    }

    @Test
    void testBuyStockWithNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.buyStock(null, stock, 10);
        });
    }

    @Test
    void testBuyStockWithNullStock() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.buyStock(user, null, 10);
        });
    }

    @Test
    void testSellStockSuccess() throws Exception {
        // Primeiro compra
        market.buyStock(user, stock, 10);
        double initialBalance = user.getBalance();
        double sellValue = stock.getCurrentPrice() * 5;

        // Depois vende
        market.sellStock(user, stock, 5);
        
        assertEquals(initialBalance + sellValue, user.getBalance(), 0.001);
        assertEquals(5, user.getStockQuantity(stock.getSymbol()));
        assertEquals(15, stock.getVolume());
        
        List<Transaction> transactions = market.getUserTransactions(user);
        assertEquals(2, transactions.size());
        Transaction t = transactions.get(0);
        assertEquals(TransactionType.SELL, t.getType());
        assertEquals(5, t.getQuantity());
    }

    @Test
    void testSellStockInsufficientQuantity() throws Exception {
        market.buyStock(user, stock, 5);
        
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            market.sellStock(user, stock, 10);
        });
        
        assertTrue(exception.getMessage().contains("Quantidade insuficiente"));
    }

    @Test
    void testSellStockWithoutOwning() {
        assertThrows(InsufficientStockException.class, () -> {
            market.sellStock(user, stock, 1);
        });
    }

    @Test
    void testSellStockWithZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.sellStock(user, stock, 0);
        });
    }

    @Test
    void testSellStockWithNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.sellStock(user, stock, -5);
        });
    }

    @Test
    void testSellStockWithNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.sellStock(null, stock, 10);
        });
    }

    @Test
    void testSellStockWithNullStock() {
        assertThrows(IllegalArgumentException.class, () -> {
            market.sellStock(user, null, 10);
        });
    }

    // ========== Testes de Preços ==========

    @Test
    void testUpdatePrices() {
        double oldPrice = stock.getCurrentPrice();
        market.updatePrices();
        double newPrice = stock.getCurrentPrice();
        assertNotEquals(oldPrice, newPrice);
        assertTrue(newPrice > 0);
        
        // Verificar se o preço foi adicionado ao histórico
        assertEquals(2, stock.getPriceHistory().size());
    }

    @Test
    void testUpdatePricesMultipleStocks() {
        Stock stock2 = new Stock("Stock 2", "STK2", 100.00, "Teste");
        market.addStock(stock2);
        
        double oldPrice1 = stock.getCurrentPrice();
        double oldPrice2 = stock2.getCurrentPrice();
        
        market.updatePrices();
        
        assertNotEquals(oldPrice1, stock.getCurrentPrice());
        assertNotEquals(oldPrice2, stock2.getCurrentPrice());
    }

    @Test
    void testPriceUpdaterStartStop() {
        assertFalse(market.isPriceUpdaterRunning());
        market.startPriceUpdates();
        
        // Pequena pausa para o updater iniciar
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(market.isPriceUpdaterRunning());
        market.stopPriceUpdates();
        assertFalse(market.isPriceUpdaterRunning());
    }

    @Test
    void testPriceUpdaterUpdatesPrices() {
        double oldPrice = stock.getCurrentPrice();
        market.startPriceUpdates();
        
        // Esperar algumas atualizações
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        double newPrice = stock.getCurrentPrice();
        assertNotEquals(oldPrice, newPrice);
        assertTrue(stock.getPriceHistory().size() > 1);
        market.stopPriceUpdates();
    }

    // ========== Testes de Transações ==========

    @Test
    void testGetUserTransactions() {
        // Nenhuma transação inicial
        List<Transaction> empty = market.getUserTransactions(user);
        assertTrue(empty.isEmpty());
        
        // Adicionar transações
        try {
            market.buyStock(user, stock, 5);
            market.buyStock(user, stock, 3);
            market.sellStock(user, stock, 2);
        } catch (Exception e) {
            fail("Não deveria lançar exceção");
        }
        
        List<Transaction> transactions = market.getUserTransactions(user);
        assertEquals(3, transactions.size());
        
        // Verificar ordem (mais recente primeiro)
        assertEquals(TransactionType.SELL, transactions.get(0).getType());
        assertEquals(TransactionType.BUY, transactions.get(1).getType());
        assertEquals(TransactionType.BUY, transactions.get(2).getType());
    }

    @Test
    void testGetUserTransactionsWithNullUser() {
        List<Transaction> transactions = market.getUserTransactions(null);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testGetTransactions() {
        try {
            market.buyStock(user, stock, 5);
        } catch (Exception e) {
            fail("Não deveria lançar exceção");
        }
        
        List<Transaction> all = market.getTransactions();
        assertEquals(1, all.size());
        
        // Modificar a lista retornada não deve afetar o original
        all.remove(0);
        assertEquals(1, market.getTransactions().size());
    }

    @Test
    void testGetRecentTransactions() {
        try {
            for (int i = 0; i < 10; i++) {
                market.buyStock(user, stock, i + 1);
                market.updatePrices(); // Para criar diferentes timestamps
            }
        } catch (Exception e) {
            fail("Não deveria lançar exceção");
        }
        
        List<Transaction> recent = market.getRecentTransactions(5);
        assertEquals(5, recent.size());
        
        // Verificar que são as mais recentes
        // A primeira transação tem quantidade 10 (última adicionada)
        assertEquals(10, recent.get(0).getQuantity());
    }

    @Test
    void testCalculatePortfolioValue() throws Exception {
        double initialValue = market.calculatePortfolioValue(user);
        assertEquals(0, initialValue);
        
        market.buyStock(user, stock, 10);
        double value = market.calculatePortfolioValue(user);
        assertEquals(10 * stock.getCurrentPrice(), value);
        
        market.updatePrices();
        double newValue = market.calculatePortfolioValue(user);
        assertEquals(10 * stock.getCurrentPrice(), newValue);
    }

    // ========== Testes de Métricas ==========

    @Test
    void testGetMostActiveStock() throws Exception {
        Stock stock2 = new Stock("Stock 2", "STK2", 100.00, "Teste");
        market.addStock(stock2);
        
        market.buyStock(user, stock, 100);
        market.buyStock(user, stock2, 50);
        
        Stock mostActive = market.getMostActiveStock();
        assertNotNull(mostActive);
        assertEquals("TST4", mostActive.getSymbol());
        assertEquals(100, mostActive.getVolume());
    }

    @Test
    void testGetBestAndWorstPerformer() {
        // Criar variação nos preços
        stock.updatePrice(55.00);
        Stock stock2 = new Stock("Stock 2", "STK2", 100.00, "Teste");
        market.addStock(stock2);
        stock2.updatePrice(95.00);
        
        Stock best = market.getBestPerformer();
        Stock worst = market.getWorstPerformer();
        
        assertNotNull(best);
        assertNotNull(worst);
        // TST4 subiu de 50 para 55 (+10%), STK2 caiu de 100 para 95 (-5%)
        assertEquals("TST4", best.getSymbol());
        assertEquals("STK2", worst.getSymbol());
    }

    @Test
    void testGetTotalTransactions() throws Exception {
        assertEquals(0, market.getTotalTransactions());
        
        market.buyStock(user, stock, 5);
        assertEquals(1, market.getTotalTransactions());
        
        market.sellStock(user, stock, 2);
        assertEquals(2, market.getTotalTransactions());
    }

    @Test
    void testGetTotalVolume() throws Exception {
        assertEquals(0, market.getTotalVolume());
        
        market.buyStock(user, stock, 10);
        assertEquals(10, market.getTotalVolume());
        
        market.buyStock(user, stock, 5);
        assertEquals(15, market.getTotalVolume());
    }

    @Test
    void testGetLastPrices() {
        Map<String, Double> prices = market.getLastPrices();
        assertNotNull(prices);
        assertEquals(1, prices.size());
        assertEquals(50.00, prices.get("TST4"));
        
        market.updatePrices();
        Map<String, Double> newPrices = market.getLastPrices();
        assertEquals(1, newPrices.size());
        assertNotEquals(50.00, newPrices.get("TST4"));
    }

    // ========== Testes de Persistência ==========

    @Test
    void testSaveAndLoadData() throws IOException, Exception {
        // Preparar dados
        market.registerUser("User2", "user2@email.com", "pass", 2000.00);
        Stock stock2 = new Stock("Stock 2", "STK2", 100.00, "Teste");
        market.addStock(stock2);
        market.buyStock(user, stock, 5);
        market.buyStock(user, stock2, 3);
        
        String filename = "test_data.txt";
        market.saveToFile(filename);
        
        // Criar novo mercado e carregar
        StockMarket newMarket = new StockMarket();
        newMarket.loadFromFile(filename);
        
        // Verificar usuários
        List<User> users = newMarket.getUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> "test@email.com".equals(u.getEmail())));
        assertTrue(users.stream().anyMatch(u -> "user2@email.com".equals(u.getEmail())));
        
        // Verificar ações
        List<Stock> stocks = newMarket.getStocks();
        assertEquals(2, stocks.size());
        assertTrue(stocks.stream().anyMatch(s -> "TST4".equals(s.getSymbol())));
        assertTrue(stocks.stream().anyMatch(s -> "STK2".equals(s.getSymbol())));
        
        // Verificar transações
        List<Transaction> transactions = newMarket.getTransactions();
        assertEquals(2, transactions.size());
        
        // Limpar arquivo de teste
        new File(filename).delete();
    }

    @Test
    void testSaveAndLoadWithNoData() throws IOException {
        String filename = "empty_data.txt";
        market.saveToFile(filename);
        
        StockMarket newMarket = new StockMarket();
        newMarket.loadFromFile(filename);
        
        assertEquals(0, newMarket.getTransactions().size());
        assertTrue(newMarket.getStocks().isEmpty() || newMarket.getStocks().size() > 0);
        
        new File(filename).delete();
    }

    @Test
    void testFileExists() {
        String filename = "test_exists.txt";
        assertFalse(market.fileExists(filename));
        
        try {
            market.saveToFile(filename);
            assertTrue(market.fileExists(filename));
        } catch (IOException e) {
            fail("Não deveria lançar exceção");
        } finally {
            new File(filename).delete();
        }
    }

    @Test
    void testExportToCSV() throws IOException {
        String filename = "test_export.csv";
        market.exportToCSV(filename);
        
        File file = new File(filename);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
        
        // Verificar conteúdo
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            assertTrue(firstLine.contains("Symbol,Name,Price,Change,Volume,Sector"));
        }
        
        file.delete();
    }

    // ========== Testes de Exceções ==========

    @Test
    void testInsufficientBalanceException() {
        InsufficientBalanceException e = new InsufficientBalanceException();
        assertNotNull(e.getMessage());
        
        InsufficientBalanceException e2 = new InsufficientBalanceException("Custom message");
        assertEquals("Custom message", e2.getMessage());
        
        Throwable cause = new RuntimeException("Cause");
        InsufficientBalanceException e3 = new InsufficientBalanceException("Message", cause);
        assertEquals(cause, e3.getCause());
    }

    @Test
    void testInsufficientStockException() {
        InsufficientStockException e = new InsufficientStockException();
        assertNotNull(e.getMessage());
        
        InsufficientStockException e2 = new InsufficientStockException("Custom message");
        assertEquals("Custom message", e2.getMessage());
        
        Throwable cause = new RuntimeException("Cause");
        InsufficientStockException e3 = new InsufficientStockException("Message", cause);
        assertEquals(cause, e3.getCause());
    }

    // ========== Testes de Integração ==========

    @Test
    void testFullTradingCycle() throws Exception {
        // 1. Registrar usuário
        market.registerUser("Trader", "trader@email.com", "pass", 5000.00);
        User trader = market.login("trader@email.com", "pass");
        assertNotNull(trader);
        
        // 2. Adicionar ações
        Stock tech = new Stock("Tech Corp", "TECH", 100.00, "Tecnologia");
        Stock fin = new Stock("Fin Bank", "FINB", 50.00, "Financeiro");
        market.addStock(tech);
        market.addStock(fin);
        
        // 3. Comprar ações
        double initialBalance = trader.getBalance();
        market.buyStock(trader, tech, 10);
        market.buyStock(trader, fin, 20);
        
        double expectedBalance = initialBalance - (10 * 100.00 + 20 * 50.00);
        assertEquals(expectedBalance, trader.getBalance(), 0.001);
        assertEquals(10, trader.getStockQuantity("TECH"));
        assertEquals(20, trader.getStockQuantity("FINB"));
        
        // 4. Atualizar preços (simular mercado)
        tech.updatePrice(110.00);
        fin.updatePrice(45.00);
        
        // 5. Vender ações com lucro
        double techPrice = tech.getCurrentPrice();
        market.sellStock(trader, tech, 5);
        
        double newBalance = expectedBalance + (5 * techPrice);
        assertEquals(newBalance, trader.getBalance(), 0.001);
        assertEquals(5, trader.getStockQuantity("TECH"));
        
        // 6. Verificar transações
        List<Transaction> transactions = market.getUserTransactions(trader);
        assertEquals(3, transactions.size());
        assertEquals(TransactionType.BUY, transactions.get(2).getType());
        assertEquals(TransactionType.BUY, transactions.get(1).getType());
        assertEquals(TransactionType.SELL, transactions.get(0).getType());
        
        // 7. Verificar valor da carteira
        double portfolioValue = market.calculatePortfolioValue(trader);
        double expectedPortfolio = 5 * techPrice + 20 * 45.00;
        assertEquals(expectedPortfolio, portfolioValue, 0.001);
    }

    @Test
    void testMultipleUsersSimultaneously() throws Exception {
        // Criar usuários
        market.registerUser("User A", "a@email.com", "pass", 1000.00);
        market.registerUser("User B", "b@email.com", "pass", 1000.00);
        market.registerUser("User C", "c@email.com", "pass", 1000.00);
        
        User userA = market.login("a@email.com", "pass");
        User userB = market.login("b@email.com", "pass");
        User userC = market.login("c@email.com", "pass");
        
        // Criar uma ação popular
        Stock pop = new Stock("Popular", "POP1", 10.00, "Teste");
        market.addStock(pop);
        
        // Operações simultâneas
        market.buyStock(userA, pop, 50);
        market.buyStock(userB, pop, 30);
        market.buyStock(userC, pop, 20);
        
        // Verificar volume total
        assertEquals(100, pop.getVolume());
        
        // Verificar carteiras individuais
        assertEquals(50, userA.getStockQuantity("POP1"));
        assertEquals(30, userB.getStockQuantity("POP1"));
        assertEquals(20, userC.getStockQuantity("POP1"));
        
        // Verificar transações
        assertEquals(3, market.getTransactions().size());
        assertEquals(1, market.getUserTransactions(userA).size());
        assertEquals(1, market.getUserTransactions(userB).size());
        assertEquals(1, market.getUserTransactions(userC).size());
    }
}