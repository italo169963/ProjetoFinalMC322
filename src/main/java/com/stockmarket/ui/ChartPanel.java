package com.stockmarket.ui;

import com.stockmarket.model.Stock;
import com.stockmarket.service.StockMarket;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

public class ChartPanel extends JPanel {
    private StockMarket market;
    private String selectedSymbol;

    public ChartPanel(StockMarket market) {
        this.market = market;
        this.selectedSymbol = null;
        setPreferredSize(new Dimension(800, 200));
        setBorder(BorderFactory.createTitledBorder("Gráfico de Preços"));
        setBackground(Color.WHITE);
    }

    public void setSelectedSymbol(String symbol) {
        this.selectedSymbol = symbol;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - 40;
        int height = getHeight() - 40;
        int x0 = 20;
        int y0 = 20;

        // Limpar área
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x0, y0, width, height);

        List<Stock> stocks = market.getStocks();
        if (stocks.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.drawString("Sem dados para exibir", x0 + 20, y0 + height / 2);
            return;
        }

        // Filtrar por símbolo selecionado ou usar todos
        List<Stock> displayedStocks = stocks;
        if (selectedSymbol != null) {
            Stock selected = market.findStock(selectedSymbol);
            if (selected != null) {
                displayedStocks = List.of(selected);
            }
        }

        // Encontrar máximo e mínimo para escala
        double maxPrice = 0;
        double minPrice = Double.MAX_VALUE;
        for (Stock stock : displayedStocks) {
            List<Double> history = stock.getPriceHistory();
            for (double price : history) {
                if (price > maxPrice) maxPrice = price;
                if (price < minPrice) minPrice = price;
            }
        }

        if (maxPrice == 0) maxPrice = 100;
        if (minPrice == Double.MAX_VALUE) minPrice = 0;
        double range = maxPrice - minPrice;
        if (range == 0) range = 1;

        // Cores para diferentes ações
        Color[] colors = {
            new Color(255, 0, 0),   // Vermelho
            new Color(0, 0, 255),   // Azul
            new Color(0, 150, 0),   // Verde
            new Color(255, 140, 0), // Laranja
            new Color(128, 0, 128), // Roxo
            new Color(0, 200, 200), // Ciano
        };

        int colorIndex = 0;

        // Desenhar grade
        g2d.setColor(new Color(200, 200, 200));
        for (int i = 0; i <= 5; i++) {
            int yPos = y0 + height - (i * height / 5);
            g2d.drawLine(x0, yPos, x0 + width, yPos);
            
            double price = minPrice + (range * i / 5);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.format("R$%.2f", price), x0 - 60, yPos + 4);
            g2d.setColor(new Color(200, 200, 200));
        }

        // Desenhar linhas para cada ação
        for (Stock stock : displayedStocks) {
            List<Double> history = stock.getPriceHistory();
            if (history.size() < 2) continue;

            Color color = colors[colorIndex % colors.length];
            g2d.setColor(color);
            colorIndex++;

            int points = history.size();
            double step = (double) width / (points - 1);

            int[] xPoints = new int[points];
            int[] yPoints = new int[points];

            for (int i = 0; i < points; i++) {
                xPoints[i] = x0 + (int)(i * step);
                double normalized = (history.get(i) - minPrice) / range;
                yPoints[i] = y0 + height - (int)(normalized * height);
            }

            // Desenhar linha
            g2d.setStroke(new BasicStroke(2));
            g2d.drawPolyline(xPoints, yPoints, points);

            // Desenhar pontos
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1));
            for (int i = 0; i < points; i += Math.max(1, points / 20)) {
                g2d.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
            }

            // Legenda
            int legendX = x0 + width - 120 + (colorIndex - 1) * 120 / Math.min(4, displayedStocks.size());
            int legendY = y0 + 20 + (colorIndex - 1) * 20;
            
            g2d.setColor(color);
            g2d.fillRect(legendX, legendY - 8, 12, 12);
            g2d.setColor(Color.BLACK);
            g2d.drawString(stock.getSymbol(), legendX + 16, legendY + 4);
            g2d.drawString(String.format("R$%.2f", stock.getCurrentPrice()), 
                legendX + 16, legendY + 20);
        }

        // Eixos
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(x0, y0, width, height);
        
        // Legendas dos eixos
        g2d.drawString("Tempo", x0 + width / 2 - 20, y0 + height + 20);
        g2d.drawString("Preço", x0 - 80, y0 + height / 2);

        // Última atualização
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.GRAY);
        g2d.drawString("Última atualização: " + 
            new java.util.Date().toString().substring(11, 19), 
            x0 + width - 120, y0 + height + 15);
    }
}