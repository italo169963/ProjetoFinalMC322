package com.stockmarket;

import com.stockmarket.ui.StockMarketUI;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Configurar para usar o Look and Feel do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Iniciar a aplicação na thread de eventos do Swing
        SwingUtilities.invokeLater(() -> {
            new StockMarketUI().setVisible(true);
        });
    }
}