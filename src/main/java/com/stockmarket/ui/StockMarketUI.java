package com.stockmarket.ui;

import com.stockmarket.model.*;
import com.stockmarket.service.StockMarket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class StockMarketUI extends JFrame {
    private StockMarket market;
    private JTable stockTable;
    private DefaultTableModel stockTableModel;
    private JLabel balanceLabel;
    private JLabel totalValueLabel;
    private JTextArea transactionArea;
    private JPanel chartPanel;
    private Timer refreshTimer;

    public StockMarketUI() {
        market = new StockMarket();
        initializeDefaultData();
        market.startPriceUpdates();
        initUI();
        
        refreshTimer = new Timer(2000, e -> refreshData());
        refreshTimer.start();
    }

    private void initializeDefaultData() {
        market.registerUser("João Silva", "joao@email.com", "123456", 10000.0);
        market.registerUser("Maria Santos", "maria@email.com", "123456", 15000.0);
        
        market.addStock(new Stock("Empresa Alpha", "ALP4", 150.50, "Tecnologia"));
        market.addStock(new Stock("Empresa Beta", "BET3", 89.75, "Financeiro"));
        market.addStock(new Stock("Empresa Gamma", "GAM5", 235.30, "Varejo"));
        market.addStock(new Stock("Empresa Delta", "DEL2", 45.90, "Saúde"));
        
        // Criar histórico de preços para testes
        for (int i = 0; i < 20; i++) {
            market.updatePrices();
        }
    }

    private void initUI() {
        setTitle("Simulador de Mercado de Ações");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Arquivo");
        JMenuItem saveItem = new JMenuItem("Salvar Dados");
        JMenuItem loadItem = new JMenuItem("Carregar Dados");
        JMenuItem exitItem = new JMenuItem("Sair");

        saveItem.addActionListener(e -> saveData());
        loadItem.addActionListener(e -> loadData());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Menu Usuário
        JMenu userMenu = new JMenu("Usuário");
        JMenuItem loginItem = new JMenuItem("Login");
        JMenuItem registerItem = new JMenuItem("Registrar");
        JMenuItem logoutItem = new JMenuItem("Logout");

        loginItem.addActionListener(e -> showLoginDialog());
        registerItem.addActionListener(e -> showRegisterDialog());
        logoutItem.addActionListener(e -> {
            market.logout();
            updateUserInfo();
        });

        userMenu.add(loginItem);
        userMenu.add(registerItem);
        userMenu.addSeparator();
        userMenu.add(logoutItem);
        menuBar.add(userMenu);

        setJMenuBar(menuBar);

        // Layout principal
        setLayout(new BorderLayout(10, 10));

        // Painel superior - informações do usuário
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.add(new JLabel("Usuário: "));
        JLabel userLabel = new JLabel("Não logado");
        userLabel.setName("userLabel");
        userPanel.add(userLabel);

        balanceLabel = new JLabel("Saldo: R$ 0,00");
        totalValueLabel = new JLabel("Total Carteira: R$ 0,00");

        topPanel.add(userPanel);
        topPanel.add(balanceLabel);
        topPanel.add(totalValueLabel);
        add(topPanel, BorderLayout.NORTH);

        // Painel central - tabela de ações e carteira
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6);

        // Tabela de ações
        String[] columns = {"Símbolo", "Nome", "Preço", "Variação", "Setor"};
        stockTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        stockTable = new JTable(stockTableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showTradeDialog();
                }
            }
        });
        JScrollPane stockScroll = new JScrollPane(stockTable);
        stockScroll.setBorder(BorderFactory.createTitledBorder("Cotações"));

        // Painel de ações
        JPanel stockPanel = new JPanel(new BorderLayout());
        stockPanel.add(stockScroll, BorderLayout.CENTER);

        JPanel stockButtons = new JPanel(new FlowLayout());
        JButton tradeButton = new JButton("Negociar");
        JButton refreshButton = new JButton("Atualizar");
        
        tradeButton.addActionListener(e -> showTradeDialog());
        refreshButton.addActionListener(e -> refreshData());
        
        stockButtons.add(tradeButton);
        stockButtons.add(refreshButton);
        stockPanel.add(stockButtons, BorderLayout.SOUTH);

        // Painel direito - carteira e transações
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // Carteira
        JTextArea portfolioArea = new JTextArea(10, 20);
        portfolioArea.setEditable(false);
        portfolioArea.setName("portfolioArea");
        JScrollPane portfolioScroll = new JScrollPane(portfolioArea);
        portfolioScroll.setBorder(BorderFactory.createTitledBorder("Carteira"));

        // Transações
        transactionArea = new JTextArea(10, 20);
        transactionArea.setEditable(false);
        transactionArea.setName("transactionArea");
        JScrollPane transactionScroll = new JScrollPane(transactionArea);
        transactionScroll.setBorder(BorderFactory.createTitledBorder("Histórico"));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setTopComponent(portfolioScroll);
        rightSplit.setBottomComponent(transactionScroll);
        rightSplit.setResizeWeight(0.5);

        rightPanel.add(rightSplit, BorderLayout.CENTER);
        splitPane.setLeftComponent(stockPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Gráfico
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart(g);
            }
        };
        chartPanel.setPreferredSize(new Dimension(800, 200));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de Preços"));
        add(chartPanel, BorderLayout.SOUTH);

        refreshData();

        // Registrar para atualização automática
        Timer timer = new Timer(3000, e -> refreshData());
        timer.start();
    }

    private void drawChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = chartPanel.getWidth() - 40;
        int height = chartPanel.getHeight() - 40;
        int x0 = 20;
        int y0 = 20;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(x0, y0, width, height);

        List<Stock> stocks = market.getStocks();
        if (stocks.isEmpty()) return;

        // Encontrar máximo e mínimo para escala
        double maxPrice = 0;
        double minPrice = Double.MAX_VALUE;
        for (Stock stock : stocks) {
            List<Double> history = stock.getPriceHistory();
            for (double price : history) {
                if (price > maxPrice) maxPrice = price;
                if (price < minPrice) minPrice = price;
            }
        }
        double range = maxPrice - minPrice;
        if (range == 0) range = 1;

        // Desenhar linhas para cada ação
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        int colorIndex = 0;

        for (Stock stock : stocks) {
            List<Double> history = stock.getPriceHistory();
            if (history.size() < 2) continue;

            g2d.setColor(colors[colorIndex % colors.length]);
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

            g2d.drawPolyline(xPoints, yPoints, points);

            // Legenda
            int legendX = x0 + width - 120 + (colorIndex - 1) * 120 / 3;
            g2d.fillRect(legendX, y0 - 15, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString(stock.getSymbol(), legendX + 15, y0 - 5);
            g2d.setColor(colors[(colorIndex - 1) % colors.length]);
        }

        // Eixos
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x0, y0, width, height);
        g2d.drawString(String.format("R$%.2f", maxPrice), x0 + 5, y0 + 15);
        g2d.drawString(String.format("R$%.2f", minPrice), x0 + 5, y0 + height - 5);
    }

    private void refreshData() {
        refreshStockTable();
        updateUserInfo();
        updatePortfolio();
        updateTransactions();
        chartPanel.repaint();
    }

    private void refreshStockTable() {
        stockTableModel.setRowCount(0);
        for (Stock stock : market.getStocks()) {
            stockTableModel.addRow(new Object[]{
                stock.getSymbol(),
                stock.getName(),
                String.format("R$ %.2f", stock.getCurrentPrice()),
                String.format("%.2f%%", stock.getPriceChange()),
                stock.getSector()
            });
        }
    }

    private void updateUserInfo() {
        User user = market.getCurrentUser();
        JLabel userLabel = (JLabel) ((JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0)).getComponent(1);
        
        if (user != null) {
            userLabel.setText(user.getName() + " (" + user.getEmail() + ")");
            balanceLabel.setText(String.format("Saldo: R$ %.2f", user.getBalance()));
            
            // Calcular valor total da carteira
            double totalValue = 0;
            var portfolio = user.getPortfolio();
            for (var entry : portfolio.entrySet()) {
                Stock stock = market.findStock(entry.getKey());
                if (stock != null) {
                    totalValue += stock.getCurrentPrice() * entry.getValue();
                }
            }
            totalValueLabel.setText(String.format("Total Carteira: R$ %.2f", totalValue + user.getBalance()));
        } else {
            userLabel.setText("Não logado");
            balanceLabel.setText("Saldo: R$ 0,00");
            totalValueLabel.setText("Total Carteira: R$ 0,00");
        }
    }

    private void updatePortfolio() {
        JTextArea portfolioArea = (JTextArea) ((JScrollPane) ((JSplitPane) ((JSplitPane) getContentPane()
            .getComponent(2)).getRightComponent()).getComponent(0)).getViewport().getView();
        
        User user = market.getCurrentUser();
        if (user == null) {
            portfolioArea.setText("Nenhum usuário logado");
            return;
        }

        StringBuilder sb = new StringBuilder();
        var portfolio = user.getPortfolio();
        if (portfolio.isEmpty()) {
            sb.append("Carteira vazia");
        } else {
            for (var entry : portfolio.entrySet()) {
                Stock stock = market.findStock(entry.getKey());
                if (stock != null) {
                    double value = stock.getCurrentPrice() * entry.getValue();
                    sb.append(String.format("%s: %d ações (R$ %.2f cada) - Total: R$ %.2f\n",
                        entry.getKey(), entry.getValue(), stock.getCurrentPrice(), value));
                } else {
                    sb.append(String.format("%s: %d ações (Ação não encontrada)\n",
                        entry.getKey(), entry.getValue()));
                }
            }
        }
        portfolioArea.setText(sb.toString());
    }

    private void updateTransactions() {
        JTextArea transArea = (JTextArea) ((JScrollPane) ((JSplitPane) ((JSplitPane) getContentPane()
            .getComponent(2)).getRightComponent()).getComponent(1)).getViewport().getView();
        
        User user = market.getCurrentUser();
        if (user == null) {
            transArea.setText("Nenhum usuário logado");
            return;
        }

        StringBuilder sb = new StringBuilder();
        var transactions = market.getTransactions();
        boolean hasTransactions = false;
        for (Transaction t : transactions) {
            if (t.getUserId().equals(user.getId())) {
                sb.append(t.toString()).append("\n");
                hasTransactions = true;
            }
        }
        if (!hasTransactions) {
            sb.append("Nenhuma transação realizada");
        }
        transArea.setText(sb.toString());
    }

    private void showLoginDialog() {
        JDialog dialog = new JDialog(this, "Login", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        dialog.add(emailField);
        dialog.add(new JLabel("Senha:"));
        JPasswordField passField = new JPasswordField();
        dialog.add(passField);

        JButton loginBtn = new JButton("Login");
        JButton cancelBtn = new JButton("Cancelar");

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passField.getPassword());
            User user = market.login(email, password);
            if (user != null) {
                JOptionPane.showMessageDialog(dialog, "Login realizado com sucesso!");
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Email ou senha inválidos!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(loginBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Registrar Usuário", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("Nome:"));
        JTextField nameField = new JTextField();
        dialog.add(nameField);
        dialog.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        dialog.add(emailField);
        dialog.add(new JLabel("Senha:"));
        JPasswordField passField = new JPasswordField();
        dialog.add(passField);
        dialog.add(new JLabel("Saldo Inicial:"));
        JTextField balanceField = new JTextField("1000.00");
        dialog.add(balanceField);

        JButton registerBtn = new JButton("Registrar");
        JButton cancelBtn = new JButton("Cancelar");

        registerBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String password = new String(passField.getPassword());
                double balance = Double.parseDouble(balanceField.getText());

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                market.registerUser(name, email, password, balance);
                JOptionPane.showMessageDialog(dialog, "Usuário registrado com sucesso!");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Saldo inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(registerBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void showTradeDialog() {
        User user = market.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Faça login primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma ação na tabela!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String symbol = (String) stockTableModel.getValueAt(selectedRow, 0);
        Stock stock = market.findStock(symbol);
        if (stock == null) return;

        JDialog dialog = new JDialog(this, "Negociar - " + symbol, true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("Ação: " + symbol));
        dialog.add(new JLabel(String.format("Preço: R$ %.2f", stock.getCurrentPrice())));
        dialog.add(new JLabel("Quantidade:"));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        dialog.add(qtySpinner);

        JButton buyBtn = new JButton("Comprar");
        JButton sellBtn = new JButton("Vender");
        JButton cancelBtn = new JButton("Cancelar");

        buyBtn.addActionListener(e -> {
            try {
                int qty = (int) qtySpinner.getValue();
                market.buyStock(user, stock, qty);
                JOptionPane.showMessageDialog(dialog, "Compra realizada com sucesso!");
                dialog.dispose();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        sellBtn.addActionListener(e -> {
            try {
                int qty = (int) qtySpinner.getValue();
                market.sellStock(user, stock, qty);
                JOptionPane.showMessageDialog(dialog, "Venda realizada com sucesso!");
                dialog.dispose();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(buyBtn);
        dialog.add(sellBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void saveData() {
        JFileChooser chooser = new JFileChooser(".");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String file = chooser.getSelectedFile().getPath();
                if (!file.endsWith(".txt")) file += ".txt";
                market.saveToFile(file);
                JOptionPane.showMessageDialog(this, "Dados salvos com sucesso!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadData() {
        JFileChooser chooser = new JFileChooser(".");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                market.loadFromFile(chooser.getSelectedFile().getPath());
                JOptionPane.showMessageDialog(this, "Dados carregados com sucesso!");
                refreshData();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new StockMarketUI().setVisible(true);
        });
    }
}