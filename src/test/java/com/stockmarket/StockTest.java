package com.stockmarket;

import com.stockmarket.model.Stock;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class StockTest {
    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock("Apple Inc.", "AAPL", 150.00, "Tecnologia");
    }

    @Test
    void testStockCreation() {
        assertNotNull(stock.getId());
        assertEquals("Apple Inc.", stock.getName());
        assertEquals("AAPL", stock.getSymbol());
        assertEquals(150.00, stock.getCurrentPrice());
        assertEquals(150.00, stock.getOpeningPrice());
        assertEquals("Tecnologia", stock.getSector());
        assertEquals(0, stock.getVolume());
        assertNotNull(stock.getPriceHistory());
        assertEquals(1, stock.getPriceHistory().size());
        assertEquals(150.00, stock.getPriceHistory().get(0));
    }

    @Test
    void testUpdatePrice() {
        stock.updatePrice(155.50);
        assertEquals(155.50, stock.getCurrentPrice());
        assertEquals(2, stock.getPriceHistory().size());
        assertEquals(155.50, stock.getPriceHistory().get(1));
    }

    @Test
    void testUpdatePriceWithNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            stock.updatePrice(-10.00);
        });
    }

    @Test
    void testUpdatePriceWithZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            stock.updatePrice(0);
        });
    }

    @Test
    void testPriceHistoryLimit() {
        // Adicionar mais de 100 preços
        for (int i = 0; i < 110; i++) {
            stock.updatePrice(150.00 + i);
        }
        
        List<Double> history = stock.getPriceHistory();
        assertEquals(100, history.size()); // Mantém apenas os últimos 100
        assertEquals(150.00 + 10, history.get(0), 0.001); // Primeiro deve ser o 11º
        assertEquals(150.00 + 109, history.get(99), 0.001); // Último deve ser o último adicionado
    }

    @Test
    void testAddVolume() {
        stock.addVolume(100);
        assertEquals(100, stock.getVolume());
        
        stock.addVolume(50);
        assertEquals(150, stock.getVolume());
        
        stock.addVolume(0);
        assertEquals(150, stock.getVolume());
        
        stock.addVolume(-10);
        assertEquals(150, stock.getVolume());
    }

    @Test
    void testSetSector() {
        stock.setSector("Financeiro");
        assertEquals("Financeiro", stock.getSector());
    }

    @Test
    void testGetPriceChange() {
        stock.updatePrice(157.50);
        double change = stock.getPriceChange();
        assertEquals(5.0, change, 0.001); // (157.50 - 150.00) / 150.00 * 100 = 5%
    }

    @Test
    void testGetPriceChangeWithSinglePrice() {
        Stock newStock = new Stock("Test", "TST", 100.00, "Teste");
        assertEquals(0, newStock.getPriceChange());
    }

    @Test
    void testGetDailyChange() {
        stock.updatePrice(157.50);
        double dailyChange = stock.getDailyChange();
        assertEquals(5.0, dailyChange, 0.001);
    }

    @Test
    void testGetMaxPrice() {
        stock.updatePrice(155.00);
        stock.updatePrice(160.00);
        stock.updatePrice(148.00);
        stock.updatePrice(165.00);
        assertEquals(165.00, stock.getMaxPrice());
    }

    @Test
    void testGetMinPrice() {
        stock.updatePrice(155.00);
        stock.updatePrice(160.00);
        stock.updatePrice(148.00);
        stock.updatePrice(165.00);
        assertEquals(148.00, stock.getMinPrice());
    }

    @Test
    void testGetAveragePrice() {
        stock.updatePrice(155.00);
        stock.updatePrice(160.00);
        stock.updatePrice(145.00);
        // Preços: 150, 155, 160, 145
        double avg = (150 + 155 + 160 + 145) / 4.0;
        assertEquals(avg, stock.getAveragePrice(), 0.001);
    }

    @Test
    void testGetPriceHistoryReturnsCopy() {
        List<Double> history = stock.getPriceHistory();
        history.add(200.00); // Modificar a cópia
        
        // O original não deve ser afetado
        assertEquals(1, stock.getPriceHistory().size());
        assertEquals(150.00, stock.getPriceHistory().get(0));
    }

    @Test
    void testToString() {
        String str = stock.toString();
        assertTrue(str.contains("AAPL"));
        assertTrue(str.contains("150.00"));
        assertTrue(str.contains("Tecnologia"));
        assertTrue(str.contains("volume=0"));
    }

    @Test
    void testConstructorWithUpperCaseSymbol() {
        Stock s = new Stock("Test", "test", 100.00, "Teste");
        assertEquals("TEST", s.getSymbol());
    }

    @Test
    void testUpdateTimestampOnPriceChange() {
        var before = stock.getUpdatedAt();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stock.updatePrice(155.00);
        var after = stock.getUpdatedAt();
        assertTrue(after.isAfter(before));
    }

    @Test
    void testUpdateTimestampOnSetSector() {
        var before = stock.getUpdatedAt();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stock.setSector("Novo Setor");
        var after = stock.getUpdatedAt();
        assertTrue(after.isAfter(before));
    }

    @Test
    void testGetFormattedDates() {
        assertNotNull(stock.getFormattedCreatedAt());
        assertNotNull(stock.getFormattedUpdatedAt());
        assertTrue(stock.getFormattedCreatedAt().contains("/"));
        assertTrue(stock.getFormattedUpdatedAt().contains("/"));
    }

    @Test
    void testEquals() {
        Stock sameStock = new Stock("Apple Inc.", "AAPL", 150.00, "Tecnologia");
        Stock differentStock = new Stock("Google", "GOOGL", 2800.00, "Tecnologia");
        
        // Mesmo símbolo mas objetos diferentes - equals usa ID, não símbolo
        assertNotEquals(stock, sameStock);
        assertNotEquals(stock, differentStock);
        assertEquals(stock, stock); // Mesmo objeto
    }

    @Test
    void testHashCode() {
        assertEquals(stock.hashCode(), stock.hashCode());
        Stock other = new Stock("Apple Inc.", "AAPL", 150.00, "Tecnologia");
        assertNotEquals(stock.hashCode(), other.hashCode());
    }
}