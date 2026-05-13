package com.minangluxe.tender.view;

import com.minangluxe.tender.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TenderFrame extends JFrame {
    private JTextArea inputArea;
    private JTextField txtPaket, txtBerat, txtHarga;
    private AIAgent ai = new AIAgent();
    private int buyerId;
    private String address;

    public TenderFrame(int buyerId, String address) {
        this.buyerId = buyerId;
        this.address = address == null || address.trim().isEmpty() ? "No Address" : address;
        
        setTitle("MinangLuxe - Smart AI Tender");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(StyleUtil.BG_DARK);

        // --- Panel Input (Atas) ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        inputArea = new JTextArea(4, 20);
        String placeholder = "Example: Need package S2, weight 500gr, budget 20k";
        inputArea.setText(placeholder);
        inputArea.setForeground(Color.GRAY);
        inputArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (inputArea.getText().equals(placeholder)) {
                    inputArea.setText("");
                    inputArea.setForeground(UIManager.getColor("TextArea.foreground"));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (inputArea.getText().isEmpty()) {
                    inputArea.setForeground(Color.GRAY);
                    inputArea.setText(placeholder);
                }
            }
        });
        inputArea.setLineWrap(true);
        
        JButton btnScan = new JButton("✨ Analyze with AI");
        StyleUtil.styleActionButton(btnScan, StyleUtil.GOLD_PRIMARY);
        
        topPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        topPanel.add(btnScan, BorderLayout.SOUTH);

        // --- Panel Hasil (Tengah) ---
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setBackground(StyleUtil.BG_CARD);
        formPanel.setBorder(BorderFactory.createTitledBorder(null, "AI Extraction Results", 0, 0, null, Color.WHITE));

        txtPaket = createStyledField("Package Type");
        txtBerat = createStyledField("Weight (gram)");
        txtHarga = createStyledField("Budget (Rp)");
        
        JButton btnSubmit = new JButton("🚀 Send Tender to Database");
        StyleUtil.styleActionButton(btnSubmit, StyleUtil.SUCCESS);

        formPanel.add(txtPaket);
        formPanel.add(txtBerat);
        formPanel.add(txtHarga);
        formPanel.add(btnSubmit);

        // --- LOGIC BUTTONS ---
        btnScan.addActionListener(e -> {
            Map<String, String> res = ai.analyzeText(inputArea.getText());
            if(!res.isEmpty()) {
                txtPaket.setText(res.getOrDefault("paket", ""));
                txtBerat.setText(res.getOrDefault("berat", ""));
                txtHarga.setText(res.getOrDefault("harga", ""));
                JOptionPane.showMessageDialog(this, "AI Successfully Extracted Data!");
            }
        });

        btnSubmit.addActionListener(e -> {
            String finalQuery = txtPaket.getText();
            if (!txtBerat.getText().isEmpty() || !txtHarga.getText().isEmpty()) {
                finalQuery += " (Weight: " + txtBerat.getText() + ", Budget: " + txtHarga.getText() + ")";
            }
            
            String sql = "INSERT INTO tender_requests (buyer_id, query, address) VALUES (?, ?, ?)";
            int status = DatabaseConnection.executeUpdate(sql, this.buyerId, finalQuery, this.address);
            if(status > 0) {
                JOptionPane.showMessageDialog(this, "AI Tender Sent! Sellers can now see your request.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send tender.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(topPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private JTextField createStyledField(String hint) {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createTitledBorder(hint));
        return f;
    }
}