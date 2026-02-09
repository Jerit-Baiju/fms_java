import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class P1 extends JFrame {
    private DefaultTableModel tableModel;
    private JTable transactionTable;
    private JLabel balanceLabel;
    private JTextField amountField, descriptionField;
    private JComboBox<String> typeComboBox;
    private double currentBalance = 0.0;

    public P1() {
        setTitle("Personal Finance Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
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
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
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
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> deleteTransaction());
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearAll());

        JButton summaryButton = new JButton("Show Summary");
        summaryButton.setBackground(new Color(52, 152, 219));
        summaryButton.setForeground(Color.WHITE);
        summaryButton.setFocusPainted(false);
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
