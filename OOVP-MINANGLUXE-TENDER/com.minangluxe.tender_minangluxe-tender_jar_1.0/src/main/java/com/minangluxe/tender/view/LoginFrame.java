package com.minangluxe.tender.view;

import com.minangluxe.tender.util.DatabaseConnection;
import com.minangluxe.tender.util.StyleUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public LoginFrame() {
        setTitle("MinangLuxe - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 420);
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleUtil.BG_DARK);

        // Panel utama (card)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(StyleUtil.BG_CARD);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel titleLabel = new JLabel("♦ MinangLuxe");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(StyleUtil.GOLD_PRIMARY);
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("Inter", Font.ITALIC, 13));
        subtitle.setForeground(StyleUtil.TEXT_MUTED);
        gbc.gridy = 1;
        mainPanel.add(subtitle, gbc);

        // Spacer
        gbc.gridy = 2;
        mainPanel.add(Box.createVerticalStrut(8), gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(StyleUtil.FONT_BODY);
        userIcon.setForeground(StyleUtil.GOLD_ACCENT);
        mainPanel.add(userIcon, gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter username");
        styleTextField(usernameField);
        mainPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 4; gbc.gridx = 0;
        JLabel passIcon = new JLabel("🔒");
        passIcon.setFont(StyleUtil.FONT_BODY);
        passIcon.setForeground(StyleUtil.GOLD_ACCENT);
        mainPanel.add(passIcon, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.putClientProperty("JTextField.placeholderText", "Enter password");
        styleTextField(passwordField);
        mainPanel.add(passwordField, gbc);

        // Role
        gbc.gridy = 5; gbc.gridx = 0;
        JLabel roleIcon = new JLabel("🎭");
        roleIcon.setForeground(StyleUtil.GOLD_ACCENT);
        mainPanel.add(roleIcon, gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"BUYER", "SELLER"});
        styleComboBox(roleCombo);
        mainPanel.add(roleCombo, gbc);

        // Buttons
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        styleButton(loginBtn, StyleUtil.GOLD_PRIMARY);
        styleButton(registerBtn, new Color(70, 70, 100));

        gbc.gridy = 6; gbc.gridx = 0;
        mainPanel.add(loginBtn, gbc);
        gbc.gridx = 1;
        mainPanel.add(registerBtn, gbc);

        add(mainPanel);

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> openRegister());
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(StyleUtil.FONT_BODY);
        tf.setBackground(StyleUtil.BG_SURFACE);
        tf.setForeground(StyleUtil.TEXT_PRIMARY);
        tf.setCaretColor(StyleUtil.GOLD_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setFont(StyleUtil.FONT_BODY);
        cb.setBackground(StyleUtil.BG_SURFACE);
        cb.setForeground(StyleUtil.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(StyleUtil.FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

private void login() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    String role = (String) roleCombo.getSelectedItem();

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Username and password cannot be empty!", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
        conn = DatabaseConnection.getConnection();
        if (conn == null || conn.isClosed()) {
            JOptionPane.showMessageDialog(this, "Login Failed! Check your database connection.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        stmt = conn.prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.setString(3, role);
        rs = stmt.executeQuery();

        if (rs.next()) {
            int userId = rs.getInt("id");
            String dbRole = rs.getString("role");
            
            JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + rs.getString("username"));
            dispose();
            
            SwingUtilities.invokeLater(() -> {
                try {
                    if ("SELLER".equalsIgnoreCase(dbRole)) {
                        new SellerDashboard(userId).setVisible(true);
                    } else {
                        new BuyerDashboard(userId).setVisible(true);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                        "Failed to open dashboard:\n" + t.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username, password, or role!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException ex) {
    ex.printStackTrace(); 
    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
} finally {
        // Tutup hanya statement dan resultset, JANGAN tutup connection
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        // Jangan tutup conn!
    }
}
    private void openRegister() {
        new RegisterFrame().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
        StyleUtil.initUIManager();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}