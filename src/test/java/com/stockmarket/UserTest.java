package com.stockmarket;

import com.stockmarket.model.*;
import com.stockmarket.exception.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Test User", "test@email.com", "password123", 1000.00);
    }

    @Test
    void testUserCreation() {
        assertNotNull(user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@email.com", user.getEmail());
        assertEquals(1000.00, user.getBalance());
        assertNotNull(user.getPortfolio());
        assertTrue(user.getPortfolio().isEmpty());
    }

    @Test
    void testSetName() {
        user.setName("New Name");
        assertEquals("New Name", user.getName());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testAddBalance() {
        user.addBalance(500.00);
        assertEquals(1500.00, user.getBalance());
        
        user.addBalance(-100.00); // Valor negativo deve ser ignorado
        assertEquals(1500.00, user.getBalance());
    }

    @Test
    void testSubtractBalanceSuccess() throws Exception {
        user.subtractBalance(300.00);
        assertEquals(700.00, user.getBalance());
    }

    @Test
    void testSubtractBalanceInsufficient() {
        assertThrows(InsufficientBalanceException.class, () -> {
            user.subtractBalance(1500.00);
        });
    }

    @Test
    void testAddStock() {
        user.addStock("AAPL", 10);
        assertEquals(10, user.getStockQuantity("AAPL"));
        assertEquals(1, user.getPortfolio().size());
        
        user.addStock("AAPL", 5);
        assertEquals(15, user.getStockQuantity("AAPL"));
        assertEquals(1, user.getPortfolio().size());
        
        user.addStock("GOOGL", 3);
        assertEquals(3, user.getStockQuantity("GOOGL"));
        assertEquals(2, user.getPortfolio().size());
    }

    @Test
    void testRemoveStockSuccess() throws Exception {
        user.addStock("AAPL", 10);
        user.removeStock("AAPL", 5);
        assertEquals(5, user.getStockQuantity("AAPL"));
        
        user.removeStock("AAPL", 5);
        assertEquals(0, user.getStockQuantity("AAPL"));
        assertFalse(user.hasStock("AAPL"));
    }

    @Test
    void testRemoveStockInsufficient() {
        user.addStock("AAPL", 5);
        assertThrows(InsufficientStockException.class, () -> {
            user.removeStock("AAPL", 10);
        });
    }

    @Test
    void testRemoveStockNotFound() {
        assertThrows(InsufficientStockException.class, () -> {
            user.removeStock("AAPL", 1);
        });
    }

    @Test
    void testHasStock() {
        assertFalse(user.hasStock("AAPL"));
        user.addStock("AAPL", 5);
        assertTrue(user.hasStock("AAPL"));
        assertFalse(user.hasStock("GOOGL"));
    }

    @Test
    void testGetStockQuantity() {
        assertEquals(0, user.getStockQuantity("AAPL"));
        user.addStock("AAPL", 7);
        assertEquals(7, user.getStockQuantity("AAPL"));
    }

    @Test
    void testGetPortfolioReturnsCopy() {
        user.addStock("AAPL", 10);
        var portfolio = user.getPortfolio();
        assertEquals(1, portfolio.size());
        
        // Modificar a cópia não deve afetar o original
        portfolio.put("GOOGL", 5);
        assertEquals(1, user.getPortfolio().size());
        assertFalse(user.hasStock("GOOGL"));
    }

    @Test
    void testGetTotalPortfolioValue() {
        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 150.00);
        prices.put("GOOGL", 2800.00);
        
        user.addStock("AAPL", 10);
        user.addStock("GOOGL", 2);
        
        double value = user.getTotalPortfolioValue(prices);
        assertEquals(10 * 150.00 + 2 * 2800.00, value);
    }

    @Test
    void testGetTotalPortfolioValueWithMissingPrice() {
        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 150.00);
        // GOOGL não está no mapa
        
        user.addStock("AAPL", 10);
        user.addStock("GOOGL", 2);
        
        double value = user.getTotalPortfolioValue(prices);
        assertEquals(10 * 150.00, value);
    }

    @Test
    void testToString() {
        user.addStock("AAPL", 5);
        String str = user.toString();
        assertTrue(str.contains("Test User"));
        assertTrue(str.contains("test@email.com"));
        assertTrue(str.contains("1000.00"));
        assertTrue(str.contains("stocks=1"));
    }

    @Test
    void testSetEmail() {
        user.setEmail("new@email.com");
        assertEquals("new@email.com", user.getEmail());
    }

    @Test
    void testSetPassword() {
        user.setPassword("newPassword");
        assertEquals("newPassword", user.getPassword());
    }

    @Test
    void testAddStockWithZeroQuantity() {
        user.addStock("AAPL", 0);
        assertFalse(user.hasStock("AAPL"));
        assertEquals(0, user.getStockQuantity("AAPL"));
    }

    @Test
    void testAddStockWithNegativeQuantity() {
        user.addStock("AAPL", -5);
        assertFalse(user.hasStock("AAPL"));
        assertEquals(0, user.getStockQuantity("AAPL"));
    }

    @Test
    void testRemoveStockWithZeroQuantity() {
        user.addStock("AAPL", 10);
        assertThrows(IllegalArgumentException.class, () -> {
            user.removeStock("AAPL", 0);
        });
    }

    @Test
    void testRemoveStockWithNegativeQuantity() {
        user.addStock("AAPL", 10);
        assertThrows(IllegalArgumentException.class, () -> {
            user.removeStock("AAPL", -5);
        });
    }
}