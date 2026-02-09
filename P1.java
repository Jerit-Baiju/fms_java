import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import java.util.Properties;

public class P1 extends JFrame {
    private DefaultTableModel tableModel;
    private JTable transactionTable;
    private JLabel balanceLabel;
    private JTextField amountField, descriptionField;
    private JComboBox<String> typeComboBox;
    private double currentBalance = 0.0;
    private Connection connection;
    
    // MySQL Configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finance_manager";
    private static final String DB_USER = "root";  // Change as needed
    private static final String DB_PASSWORD = "";  // Change as needed

    public P1() {
        setTitle("Personal Finance Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initDatabase();
        initComponents();
        loadTransactionsFromDB();
        setVisible(true);
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Balance display
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(70, 130, 180));
        balanceLabel = new JLabel("Current Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 24));
        balanceLabel.setForeground(Color.WHITE);
        topPanel.add(balanceLabel);

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Transaction"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 1;
        typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});
        inputPanel.add(typeComboBox, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        descriptionField = new JTextField(20);
        inputPanel.add(descriptionField, gbc);

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        amountField = new JTextField(20);
        inputPanel.add(amountField, gbc);

        // Add button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("Add Transaction");
        addButton.setBackground(new Color(27, 133, 73));
        addButton.setForeground(Color.WHITE);
        addButton.setOpaque(true);
        addButton.setContentAreaFilled(true);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(200, 35));
        addButton.addActionListener(e -> addTransaction());
        inputPanel.add(addButton, gbc);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Transaction History"));

        String[] columnNames = {"Date", "Type", "Description", "Amount", "Balance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.setContentAreaFilled(true);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.addActionListener(e -> deleteTransaction());
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setBackground(new Color(70, 80, 82));
        clearButton.setForeground(Color.WHITE);
        clearButton.setOpaque(true);
        clearButton.setContentAreaFilled(true);
        clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setPreferredSize(new Dimension(150, 40));
        clearButton.addActionListener(e -> clearAll());

        JButton summaryButton = new JButton("Show Summary");
        summaryButton.setBackground(new Color(52, 152, 219));
        summaryButton.setForeground(Color.WHITE);
        summaryButton.setOpaque(true);
        summaryButton.setContentAreaFilled(true);
        summaryButton.setBorderPainted(false);
        summaryButton.setFocusPainted(false);
        summaryButton.setFont(new Font("Arial", Font.BOLD, 14));
        summaryButton.setPreferredSize(new Dimension(150, 40));
        summaryButton.addActionListener(e -> showSummary());

        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(summaryButton);

        // Assemble main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.WEST);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void initDatabase() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First, create database if it doesn't exist
            createDatabaseIfNotExists();
            
            // Now connect to the specific database
            String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                DB_HOST, DB_PORT, DB_NAME);
            connection = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            
            // Create table if not exists (MySQL syntax)
            String createTableSQL = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "date VARCHAR(50) NOT NULL," +
                "type VARCHAR(20) NOT NULL," +
                "description VARCHAR(255) NOT NULL," +
                "amount DOUBLE NOT NULL," +
                "balance DOUBLE NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            Statement stmt = connection.createStatement();
            stmt.execute(createTableSQL);
            stmt.close();
            
            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database initialization failed: " + e.getMessage() + 
                "\n\nPlease ensure MySQL is running and credentials are correct.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createDatabaseIfNotExists() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Connect to MySQL server without specifying database
            String jdbcUrl = String.format("jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                DB_HOST, DB_PORT);
            conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            
            // Create database if it doesn't exist
            stmt = conn.createStatement();
            String createDbSQL = "CREATE DATABASE IF NOT EXISTS " + DB_NAME + 
                " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.executeUpdate(createDbSQL);
            System.out.println("Database '" + DB_NAME + "' is ready.");
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create database: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTransactionsFromDB() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY id");
            
            while (rs.next()) {
                String date = rs.getString("date");
                String type = rs.getString("type");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                double balance = rs.getDouble("balance");
                
                Object[] rowData = {
                    date,
                    type,
                    description,
                    String.format("$%.2f", amount),
                    String.format("$%.2f", balance)
                };
                tableModel.addRow(rowData);
                currentBalance = balance;
            }
            
            rs.close();
            stmt.close();
            updateBalanceLabel();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load transactions: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTransactionToDB(String date, String type, String description, double amount, double balance) {
        try {
            String insertSQL = "INSERT INTO transactions (date, type, description, amount, balance) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertSQL);
            pstmt.setString(1, date);
            pstmt.setString(2, type);
            pstmt.setString(3, description);
            pstmt.setDouble(4, amount);
            pstmt.setDouble(5, balance);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save transaction: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTransactionFromDB(int rowIndex) {
        try {
            // Get the transaction date and description to identify it uniquely
            String date = (String) tableModel.getValueAt(rowIndex, 0);
            String description = (String) tableModel.getValueAt(rowIndex, 2);
            
            String deleteSQL = "DELETE FROM transactions WHERE id = (" +
                "SELECT id FROM transactions WHERE date = ? AND description = ? LIMIT 1)";
            PreparedStatement pstmt = connection.prepareStatement(deleteSQL);
            pstmt.setString(1, date);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Update all subsequent balances in the database
            updateBalancesInDB();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete transaction: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBalancesInDB() {
        try {
            // Clear and re-insert all transactions with updated balances
            connection.createStatement().execute("DELETE FROM transactions");
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String date = (String) tableModel.getValueAt(i, 0);
                String type = (String) tableModel.getValueAt(i, 1);
                String description = (String) tableModel.getValueAt(i, 2);
                String amountStr = (String) tableModel.getValueAt(i, 3);
                String balanceStr = (String) tableModel.getValueAt(i, 4);
                
                double amount = Double.parseDouble(amountStr.substring(1));
                double balance = Double.parseDouble(balanceStr.substring(1));
                
                saveTransactionToDB(date, type, description, amount, balance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearAllFromDB() {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("DELETE FROM transactions");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to clear database: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTransaction() {
        try {
            String description = descriptionField.getText().trim();
            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a description", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String type = (String) typeComboBox.getSelectedItem();
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

            if (type.equals("Income")) {
                currentBalance += amount;
            } else {
                currentBalance -= amount;
            }

            Object[] rowData = {
                date,
                type,
                description,
                String.format("$%.2f", amount),
                String.format("$%.2f", currentBalance)
            };

            tableModel.addRow(rowData);
            saveTransactionToDB(date, type, description, amount, currentBalance);
            updateBalanceLabel();
            clearInputFields();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this transaction?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Recalculate balance
            String type = (String) tableModel.getValueAt(selectedRow, 1);
            String amountStr = (String) tableModel.getValueAt(selectedRow, 3);
            double amount = Double.parseDouble(amountStr.substring(1));

            if (type.equals("Income")) {
                currentBalance -= amount;
            } else {
                currentBalance += amount;
            }

            deleteTransactionFromDB(selectedRow);
            tableModel.removeRow(selectedRow);
            recalculateBalances();
            updateBalanceLabel();
        }
    }

    private void recalculateBalances() {
        double balance = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String type = (String) tableModel.getValueAt(i, 1);
            String amountStr = (String) tableModel.getValueAt(i, 3);
            double amount = Double.parseDouble(amountStr.substring(1));

            if (type.equals("Income")) {
                balance += amount;
            } else {
                balance -= amount;
            }

            tableModel.setValueAt(String.format("$%.2f", balance), i, 4);
        }
        currentBalance = balance;
    }

    private void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear all transactions?", 
            "Confirm Clear", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            clearAllFromDB();
            tableModel.setRowCount(0);
            currentBalance = 0.0;
            updateBalanceLabel();
        }
    }

    private void showSummary() {
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        int incomeCount = 0;
        int expenseCount = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String type = (String) tableModel.getValueAt(i, 1);
            String amountStr = (String) tableModel.getValueAt(i, 3);
            double amount = Double.parseDouble(amountStr.substring(1));

            if (type.equals("Income")) {
                totalIncome += amount;
                incomeCount++;
            } else {
                totalExpense += amount;
                expenseCount++;
            }
        }

        String summary = String.format(
            "Financial Summary\n\n" +
            "Total Income: $%.2f (%d transactions)\n" +
            "Total Expenses: $%.2f (%d transactions)\n" +
            "Net Balance: $%.2f\n\n" +
            "Total Transactions: %d",
            totalIncome, incomeCount,
            totalExpense, expenseCount,
            currentBalance,
            tableModel.getRowCount()
        );

        JOptionPane.showMessageDialog(this, summary, "Financial Summary", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateBalanceLabel() {
        balanceLabel.setText(String.format("Current Balance: $%.2f", currentBalance));
        if (currentBalance >= 0) {
            balanceLabel.setForeground(Color.WHITE);
        } else {
            balanceLabel.setForeground(new Color(255, 100, 100));
        }
    }

    private void clearInputFields() {
        descriptionField.setText("");
        amountField.setText("");
        descriptionField.requestFocus();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new P1());
    }
}
