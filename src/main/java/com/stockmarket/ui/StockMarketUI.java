package com.stockmarket.ui;

import com.stockmarket.model.*;
import com.stockmarket.service.StockMarket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class StockMarketUI extends JFrame {
    private StockMarket market;
    private JTable stockTable;
    private DefaultTableModel stockTableModel;
    private JLabel balanceLabel;
    private JLabel totalValueLabel;
    private JLabel userLabel;
    private JTextArea portfolioArea;
    private JTextArea transactionArea;
    private ChartPanel chartPanel;
    private Timer refreshTimer;

    public StockMarketUI() {
        market = new StockMarket();
        initializeDefaultData();
        market.startPriceUpdates();
        initUI();
        
        refreshTimer = new Timer(2000, e -> refreshData());
        refreshTimer.start();

        // Adicionar listener para fechamento
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                market.stopPriceUpdates();
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
            }
        });
    }

    private void initializeDefaultData() {
        try {
            market.registerUser("João Silva", "joao@email.com", "123456", 10000.0);
            market.registerUser("Maria Santos", "maria@email.com", "123456", 15000.0);
            market.registerUser("Pedro Oliveira", "pedro@email.com", "123456", 8000.0);
            
            // Ações com diferentes setores
            market.addStock(new Stock("Alpha Tech", "ALP4", 150.50, "Tecnologia"));
            market.addStock(new Stock("Beta Bank", "BET3", 89.75, "Financeiro"));
            market.addStock(new Stock("Gamma Retail", "GAM5", 235.30, "Varejo"));
            market.addStock(new Stock("Delta Health", "DEL2", 45.90, "Saúde"));
            market.addStock(new Stock("Epsilon Energy", "EPS1", 320.00, "Energia"));
            market.addStock(new Stock("Zeta Telecom", "ZET6", 67.80, "Telecom"));
            
            // Criar histórico de preços para gráficos
            for (int i = 0; i < 30; i++) {
                market.updatePrices();
            }
        } catch (Exception e) {
            System.err.println("Erro ao inicializar dados: " + e.getMessage());
        }
    }

    private void initUI() {
        setTitle("Simulador de Mercado de Ações - MC322");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Criar menu
        createMenuBar();

        // Painel superior - informações do usuário
        createTopPanel();

        // Painel central - split com tabela e carteira
        createCenterPanel();

        // Painel inferior - gráfico
        createBottomPanel();

        // Atualizar dados inicialmente
        refreshData();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Arquivo
        JMenu fileMenu = new JMenu("Arquivo");
        JMenuItem saveItem = new JMenuItem("Salvar Dados");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveItem.addActionListener(e -> saveData());

        JMenuItem loadItem = new JMenuItem("Carregar Dados");
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        loadItem.addActionListener(e -> loadData());

        JMenuItem exportItem = new JMenuItem("Exportar CSV");
        exportItem.addActionListener(e -> exportData());

        JMenuItem exitItem = new JMenuItem("Sair");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Menu Usuário
        JMenu userMenu = new JMenu("Usuário");
        JMenuItem loginItem = new JMenuItem("Login");
        loginItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        loginItem.addActionListener(e -> showLoginDialog());

        JMenuItem registerItem = new JMenuItem("Registrar");
        registerItem.addActionListener(e -> showRegisterDialog());

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            market.logout();
            updateUserInfo();
            JOptionPane.showMessageDialog(this, "Logout realizado com sucesso!");
        });

        userMenu.add(loginItem);
        userMenu.add(registerItem);
        userMenu.addSeparator();
        userMenu.add(logoutItem);
        menuBar.add(userMenu);

        // Menu Ações
        JMenu stocksMenu = new JMenu("Ações");
        JMenuItem addStockItem = new JMenuItem("Adicionar Ação");
        addStockItem.addActionListener(e -> showAddStockDialog());

        JMenuItem updatePricesItem = new JMenuItem("Atualizar Preços");
        updatePricesItem.addActionListener(e -> {
            market.updatePrices();
            refreshData();
            JOptionPane.showMessageDialog(this, "Preços atualizados!");
        });

        stocksMenu.add(addStockItem);
        stocksMenu.add(updatePricesItem);
        menuBar.add(stocksMenu);

        // Menu Ajuda
        JMenu helpMenu = new JMenu("Ajuda");
        JMenuItem aboutItem = new JMenuItem("Sobre");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(1, 4, 15, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(240, 240, 240));

        userLabel = new JLabel("Usuário: Não logado");
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        balanceLabel = new JLabel("Saldo: R$ 0,00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        totalValueLabel = new JLabel("Patrimônio: R$ 0,00");
        totalValueLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel statusLabel = new JLabel("Status: Online");
        statusLabel.setForeground(new Color(0, 150, 0));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        topPanel.add(userLabel);
        topPanel.add(balanceLabel);
        topPanel.add(totalValueLabel);
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createCenterPanel() {
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.6);
        mainSplit.setDividerSize(5);

        // Painel esquerdo - Tabela de ações
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        String[] columns = {"Símbolo", "Nome", "Preço", "Variação", "Setor", "Volume"};
        stockTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        stockTable = new JTable(stockTableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setRowHeight(25);
        stockTable.getTableHeader().setReorderingAllowed(false);
        
        stockTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showTradeDialog();
                }
            }
        });

        JScrollPane stockScroll = new JScrollPane(stockTable);
        stockScroll.setBorder(BorderFactory.createTitledBorder("Cotações em Tempo Real"));

        JPanel stockButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton tradeButton = new JButton("Negociar");
        tradeButton.setPreferredSize(new Dimension(100, 30));
        tradeButton.addActionListener(e -> showTradeDialog());

        JButton refreshButton = new JButton("Atualizar");
        refreshButton.setPreferredSize(new Dimension(100, 30));
        refreshButton.addActionListener(e -> refreshData());

        stockButtonPanel.add(tradeButton);
        stockButtonPanel.add(refreshButton);

        leftPanel.add(stockScroll, BorderLayout.CENTER);
        leftPanel.add(stockButtonPanel, BorderLayout.SOUTH);

        // Painel direito - Carteira e transações
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        portfolioArea = new JTextArea(8, 20);
        portfolioArea.setEditable(false);
        portfolioArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane portfolioScroll = new JScrollPane(portfolioArea);
        portfolioScroll.setBorder(BorderFactory.createTitledBorder("Minha Carteira"));

        transactionArea = new JTextArea(8, 20);
        transactionArea.setEditable(false);
        transactionArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane transactionScroll = new JScrollPane(transactionArea);
        transactionScroll.setBorder(BorderFactory.createTitledBorder("Histórico de Transações"));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setTopComponent(portfolioScroll);
        rightSplit.setBottomComponent(transactionScroll);
        rightSplit.setResizeWeight(0.5);
        rightSplit.setDividerSize(5);

        rightPanel.add(rightSplit, BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void createBottomPanel() {
        chartPanel = new ChartPanel(market);
        add(chartPanel, BorderLayout.SOUTH);
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
            String change = String.format("%+.2f%%", stock.getPriceChange());
            // Cor da variação
            if (stock.getPriceChange() > 0) {
                stockTableModel.addRow(new Object[]{
                    stock.getSymbol(),
                    stock.getName(),
                    String.format("R$ %.2f", stock.getCurrentPrice()),
                    change,
                    stock.getSector(),
                    stock.getVolume()
                });
            } else {
                stockTableModel.addRow(new Object[]{
                    stock.getSymbol(),
                    stock.getName(),
                    String.format("R$ %.2f", stock.getCurrentPrice()),
                    change,
                    stock.getSector(),
                    stock.getVolume()
                });
            }
        }
    }

    private void updateUserInfo() {
        User user = market.getCurrentUser();
        if (user != null) {
            userLabel.setText("Usuário: " + user.getName() + " (" + user.getEmail() + ")");
            balanceLabel.setText(String.format("Saldo: R$ %.2f", user.getBalance()));
            
            double portfolioValue = market.calculatePortfolioValue(user);
            double total = user.getBalance() + portfolioValue;
            totalValueLabel.setText(String.format("Patrimônio: R$ %.2f (Carteira: R$ %.2f)", 
                total, portfolioValue));
        } else {
            userLabel.setText("Usuário: Não logado");
            balanceLabel.setText("Saldo: R$ 0,00");
            totalValueLabel.setText("Patrimônio: R$ 0,00");
        }
    }

    private void updatePortfolio() {
        User user = market.getCurrentUser();
        if (user == null) {
            portfolioArea.setText("Nenhum usuário logado.");
            return;
        }

        var portfolio = user.getPortfolio();
        if (portfolio.isEmpty()) {
            portfolioArea.setText("Carteira vazia.\n\nUse a opção 'Negociar' para comprar ações.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Carteira de %s\n", user.getName()));
        sb.append("=".repeat(50)).append("\n\n");
        
        double totalValue = 0;
        for (var entry : portfolio.entrySet()) {
            Stock stock = market.findStock(entry.getKey());
            if (stock != null) {
                double value = stock.getCurrentPrice() * entry.getValue();
                totalValue += value;
                sb.append(String.format("%-6s %4d ações x R$%7.2f = R$%8.2f\n",
                    entry.getKey(), entry.getValue(), 
                    stock.getCurrentPrice(), value));
            }
        }
        sb.append("\n").append("=".repeat(50)).append("\n");
        sb.append(String.format("Total Carteira: R$ %.2f\n", totalValue));
        portfolioArea.setText(sb.toString());
    }

    private void updateTransactions() {
        User user = market.getCurrentUser();
        if (user == null) {
            transactionArea.setText("Nenhum usuário logado.");
            return;
        }

        List<Transaction> userTransactions = market.getUserTransactions(user);
        if (userTransactions.isEmpty()) {
            transactionArea.setText("Nenhuma transação realizada.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Histórico de %s\n", user.getName()));
        sb.append("=".repeat(50)).append("\n\n");
        
        for (Transaction t : userTransactions) {
            sb.append(t.toString()).append("\n");
        }
        transactionArea.setText(sb.toString());
    }

    // ========== Diálogos ==========

    private void showLoginDialog() {
        JDialog dialog = new JDialog(this, "Login", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        dialog.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        dialog.add(passField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(100, 30));
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setPreferredSize(new Dimension(100, 30));

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passField.getPassword());
            User user = market.login(email, password);
            if (user != null) {
                JOptionPane.showMessageDialog(dialog, 
                    "Bem-vindo, " + user.getName() + "!", 
                    "Login", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Email ou senha inválidos!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Registrar Usuário", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        dialog.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        dialog.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Saldo Inicial:"), gbc);
        gbc.gridx = 1;
        JTextField balanceField = new JTextField("1000.00", 15);
        dialog.add(balanceField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton registerBtn = new JButton("Registrar");
        registerBtn.setPreferredSize(new Dimension(100, 30));
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setPreferredSize(new Dimension(100, 30));

        registerBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String password = new String(passField.getPassword());
                double balance = Double.parseDouble(balanceField.getText().trim());

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Preencha todos os campos!", 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (balance < 0) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Saldo não pode ser negativo!", 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                market.registerUser(name, email, password, balance);
                JOptionPane.showMessageDialog(dialog, 
                    "Usuário registrado com sucesso!\nFaça login para continuar.", 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Saldo inválido!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void showTradeDialog() {
        User user = market.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, 
                "Faça login primeiro!", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Selecione uma ação na tabela!", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String symbol = (String) stockTableModel.getValueAt(selectedRow, 0);
        Stock stock = market.findStock(symbol);
        if (stock == null) {
            JOptionPane.showMessageDialog(this, 
                "Ação não encontrada!", 
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Negociar - " + symbol, true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        // Informações da ação
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel(
            String.format("<html><b>%s</b> - %s<br>Preço: R$ %.2f | Saldo: R$ %.2f</html>",
                symbol, stock.getName(), stock.getCurrentPrice(), user.getBalance()),
            SwingConstants.CENTER
        );
        dialog.add(infoLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        dialog.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1;
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        qtySpinner.setPreferredSize(new Dimension(100, 25));
        dialog.add(qtySpinner, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton buyBtn = new JButton("Comprar");
        buyBtn.setPreferredSize(new Dimension(100, 30));
        buyBtn.setBackground(new Color(0, 150, 0));
        buyBtn.setForeground(Color.WHITE);

        JButton sellBtn = new JButton("Vender");
        sellBtn.setPreferredSize(new Dimension(100, 30));
        sellBtn.setBackground(new Color(200, 0, 0));
        sellBtn.setForeground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setPreferredSize(new Dimension(100, 30));

        buyBtn.addActionListener(e -> {
            try {
                int qty = (int) qtySpinner.getValue();
                market.buyStock(user, stock, qty);
                JOptionPane.showMessageDialog(dialog, 
                    String.format("Compra realizada!\n%d ações de %s.", qty, symbol),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        sellBtn.addActionListener(e -> {
            try {
                int qty = (int) qtySpinner.getValue();
                market.sellStock(user, stock, qty);
                JOptionPane.showMessageDialog(dialog, 
                    String.format("Venda realizada!\n%d ações de %s.", qty, symbol),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        buttonPanel.add(buyBtn);
        buttonPanel.add(sellBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void showAddStockDialog() {
        JDialog dialog = new JDialog(this, "Adicionar Ação", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Símbolo:"), gbc);
        gbc.gridx = 1;
        JTextField symbolField = new JTextField(15);
        dialog.add(symbolField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Preço Inicial:"), gbc);
        gbc.gridx = 1;
        JTextField priceField = new JTextField("100.00", 15);
        dialog.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Setor:"), gbc);
        gbc.gridx = 1;
        String[] sectors = {"Tecnologia", "Financeiro", "Varejo", "Saúde", "Energia", "Telecom", "Outros"};
        JComboBox<String> sectorCombo = new JComboBox<>(sectors);
        dialog.add(sectorCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addBtn = new JButton("Adicionar");
        addBtn.setPreferredSize(new Dimension(100, 30));
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setPreferredSize(new Dimension(100, 30));

        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String symbol = symbolField.getText().trim().toUpperCase();
                double price = Double.parseDouble(priceField.getText().trim());
                String sector = (String) sectorCombo.getSelectedItem();

                if (name.isEmpty() || symbol.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Preencha todos os campos!", 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Stock stock = new Stock(name, symbol, price, sector);
                market.addStock(stock);
                JOptionPane.showMessageDialog(dialog, 
                    "Ação adicionada com sucesso!", 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Preço inválido!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        buttonPanel.add(addBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "<html><h2>Simulador de Mercado de Ações</h2>" +
            "<p>Versão 1.0.0</p>" +
            "<p>MC322 - Programação Orientada a Objetos</p>" +
            "<p>UNICAMP - 2026</p>" +
            "<hr>" +
            "<p>Sistema para simulação de operações no mercado de ações.</p>" +
            "<p>Principais funcionalidades:</p>" +
            "<ul>" +
            "<li>Compra e venda de ações</li>" +
            "<li>Atualização automática de preços</li>" +
            "<li>Gráficos de desempenho</li>" +
            "<li>Histórico de transações</li>" +
            "<li>Persistência de dados</li>" +
            "</ul></html>",
            "Sobre",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ========== Persistência ==========

    private void saveData() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new java.io.File("market_data.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String file = chooser.getSelectedFile().getPath();
                if (!file.endsWith(".txt")) file += ".txt";
                market.saveToFile(file);
                JOptionPane.showMessageDialog(this, 
                    "Dados salvos em: " + file, 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao salvar: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadData() {
        JFileChooser chooser = new JFileChooser(".");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                market.loadFromFile(chooser.getSelectedFile().getPath());
                JOptionPane.showMessageDialog(this, 
                    "Dados carregados com sucesso!", 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportData() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new java.io.File("export.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String file = chooser.getSelectedFile().getPath();
                if (!file.endsWith(".csv")) file += ".csv";
                market.exportToCSV(file);
                JOptionPane.showMessageDialog(this, 
                    "Dados exportados para: " + file, 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao exportar: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(this,
            "Deseja salvar os dados antes de sair?",
            "Sair",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            saveData();
        } else if (option == JOptionPane.CANCEL_OPTION) {
            return;
        }

        market.stopPriceUpdates();
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        System.exit(0);
    }

    // ========== Main ==========

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