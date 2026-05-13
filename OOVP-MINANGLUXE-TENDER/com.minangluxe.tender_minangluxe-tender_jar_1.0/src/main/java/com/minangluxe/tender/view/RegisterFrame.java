package com.minangluxe.tender.view;

import com.minangluxe.tender.util.DatabaseConnection;
import com.minangluxe.tender.util.StyleUtil;
import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField usernameField, phoneField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public RegisterFrame() {
        setTitle("MinangLuxe - Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleUtil.BG_DARK);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(StyleUtil.BG_CARD);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("♦ Create New Account");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(StyleUtil.GOLD_PRIMARY);
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(title, gbc);

        gbc.gridy = 1;
        mainPanel.add(Box.createVerticalStrut(8), gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(createLabel("👤 Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter username");
        styleTextField(usernameField);
        mainPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 3; gbc.gridx = 0;
        mainPanel.add(createLabel("🔒 Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.putClientProperty("JTextField.placeholderText", "Enter password");
        styleTextField(passwordField);
        mainPanel.add(passwordField, gbc);

        // Phone
        gbc.gridy = 4; gbc.gridx = 0;
        mainPanel.add(createLabel("📞 Phone No:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(15);
        phoneField.putClientProperty("JTextField.placeholderText", "Enter phone number");
        styleTextField(phoneField);
        mainPanel.add(phoneField, gbc);

        // Role
        gbc.gridy = 5; gbc.gridx = 0;
        mainPanel.add(createLabel("🎭 Register as:"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"BUYER", "SELLER"});
        styleComboBox(roleCombo);
        mainPanel.add(roleCombo, gbc);

        // Buttons
        JButton daftarBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");
        styleButton(daftarBtn, StyleUtil.SUCCESS);
        styleButton(backBtn, new Color(100, 100, 120));

        gbc.gridy = 6; gbc.gridx = 0;
        mainPanel.add(daftarBtn, gbc);
        gbc.gridx = 1;
        mainPanel.add(backBtn, gbc);

        add(mainPanel);

        daftarBtn.addActionListener(e -> register());
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(StyleUtil.FONT_LABEL);
        lbl.setForeground(StyleUtil.TEXT_MUTED);
        return lbl;
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

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String phone = phoneField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "INSERT INTO users (username, password, role, phone) VALUES (?, ?, ?, ?)";
        int result = DatabaseConnection.executeUpdate(query, username, password, role, phone);
        if (result > 0) {
            JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}