package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.DatabaseConnection;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuyerPanel extends JPanel implements TenderController.TenderListener {

    private JPanel chatContent;
    private JScrollPane chatScroll;
    private JTextField requestInput;
    private JTextField addressInput;
    private JButton sendButton;
    private JButton btnOpenAI;

    private JPanel cartItemsPanel;
    private JLabel totalLabel;
    private JButton checkoutButton;
    private final List<Offer> selectedOffers = new ArrayList<>();

    public BuyerPanel(int userId) {
        final int buyerId = userId; // local capture for lambda
        setLayout(new BorderLayout(0, 0));
        setBackground(StyleUtil.BG_DARK);
        TenderController.getInstance().addListener(this);

        // --- AREA CHAT (TENGAH) ---
        chatContent = new JPanel();
        chatContent.setLayout(new BoxLayout(chatContent, BoxLayout.Y_AXIS));
        chatContent.setBackground(StyleUtil.BG_DARK);
        chatContent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        chatScroll = new JScrollPane(chatContent);
        chatScroll.setBorder(null);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatScroll.getViewport().setBackground(StyleUtil.BG_DARK);

        // --- AREA INPUT (BAWAH) ---
        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBackground(StyleUtil.BG_CARD);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Field Alamat & Pesanan
        JPanel fields = new JPanel(new GridLayout(2, 1, 5, 5));
        fields.setOpaque(false);
        
        addressInput = new JTextField();
        addressInput.putClientProperty("JTextField.placeholderText", "Enter shipping address");
        addressInput.setBorder(BorderFactory.createTitledBorder(null, "📍 Shipping Address", 0, 0, null, Color.GRAY));

        requestInput = new JTextField();
        requestInput.putClientProperty("JTextField.placeholderText", "What are you looking for today?");
        requestInput.setBorder(BorderFactory.createTitledBorder(null, "💬 New Tender Request", 0, 0, null, Color.GRAY));
        
        fields.add(addressInput);
        fields.add(requestInput);

        // Tombol-Tombol Action
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        // 🤖 INTEGRASI TOMBOL AI
        btnOpenAI = new JButton("🤖 Ask AI Tender");
        StyleUtil.styleActionButton(btnOpenAI, StyleUtil.GOLD_PRIMARY);
        btnOpenAI.setToolTipText("Use AI for automatic input");
        
        sendButton = new JButton("Send Tender 🚀");
        StyleUtil.styleActionButton(sendButton, StyleUtil.SUCCESS);

        JButton btnReset = new JButton("↺ Reset");
        StyleUtil.styleLinkButton(btnReset);
        btnReset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to PERMANENTLY clear the chat and all offers?", 
                "Permanent Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                com.minangluxe.tender.util.DatabaseConnection.executeUpdate("DELETE FROM tender_offers");
                com.minangluxe.tender.util.DatabaseConnection.executeUpdate("DELETE FROM tender_requests");
                chatContent.removeAll();
                chatContent.revalidate();
                chatContent.repaint();
                loadedOfferIds.clear();
                JOptionPane.showMessageDialog(this, "All records have been cleared.");
            }
        });

        actions.add(btnReset);
        actions.add(btnOpenAI);
        actions.add(sendButton);

        inputPanel.add(fields, BorderLayout.CENTER);
        inputPanel.add(actions, BorderLayout.SOUTH);

        // --- SIDEBAR KERANJANG (KANAN) ---
        JPanel sidebar = createCartSidebar();

        add(chatScroll, BorderLayout.CENTER);
        add(sidebar, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // --- LOGIC INTERACTION ---
        
        // Klik Tombol AI
        btnOpenAI.addActionListener(e -> {
            TenderFrame tf = new TenderFrame(buyerId, addressInput.getText());
            tf.setVisible(true);
        });

        // Klik Kirim Manual
        sendButton.addActionListener(e -> {
            String query = requestInput.getText().trim();
            String addr = addressInput.getText().trim();
            if (!query.isEmpty()) {
                // Save to DB so Seller (other process) can see it
                DatabaseConnection.executeUpdate(
                    "INSERT INTO tender_requests (buyer_id, query, address) VALUES (?, ?, ?)",
                    buyerId, query, addr);
                // Also notify in-process listeners (same JVM)
                TenderController.getInstance().postRequest(query, "Standard", addr);
                requestInput.setText("");
                addChatMessage("YOU", query);
            }
        });

        // Enter di input field
        requestInput.addActionListener(e -> sendButton.doClick());

        // Poll for new offers from DB (for split-screen interaction)
        javax.swing.Timer pollTimer = new javax.swing.Timer(3000, e -> loadOffersFromDB());
        pollTimer.start();
    }

    private Set<Integer> loadedOfferIds = new HashSet<>();

    private void loadOffersFromDB() {
        new Thread(() -> {
            String sql = "SELECT id, request_id, seller_name, product_name, price, quantity, rating " +
                         "FROM tender_offers ORDER BY created_at DESC LIMIT 20";
            java.sql.Connection conn = DatabaseConnection.getConnection();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    if (!loadedOfferIds.contains(id)) {
                        loadedOfferIds.add(id);
                        String seller = rs.getString("seller_name");
                        String product = rs.getString("product_name");
                        double price = rs.getDouble("price");
                        int quantity = rs.getInt("quantity");
                        
                        SwingUtilities.invokeLater(() -> {
                            addOfferMessage(seller.toUpperCase(), product, price, quantity);
                        });
                    }
                }
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private JPanel createCartSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(StyleUtil.BG_CARD);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, StyleUtil.BG_ELEVATED));

        JLabel title = new JLabel("🛒 Tender Cart");
        title.setFont(StyleUtil.FONT_BOLD);
        title.setForeground(StyleUtil.GOLD_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(StyleUtil.BG_CARD);
        
        JScrollPane scroll = new JScrollPane(cartItemsPanel);
        scroll.setBorder(null);

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        totalLabel = new JLabel("Total: Rp 0");
        totalLabel.setFont(StyleUtil.FONT_TITLE);
        totalLabel.setForeground(Color.WHITE);
        
        checkoutButton = new JButton("Checkout Now");
        StyleUtil.styleActionButton(checkoutButton, StyleUtil.SUCCESS);
        checkoutButton.addActionListener(e -> handleCheckout());

        footer.add(totalLabel, BorderLayout.NORTH);
        footer.add(checkoutButton, BorderLayout.SOUTH);

        sidebar.add(title, BorderLayout.NORTH);
        sidebar.add(scroll, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);
        
        return sidebar;
    }

    private void addOfferMessage(String seller, String product, double price, int quantity) {
        JPanel msg = new JPanel(new BorderLayout(10, 0));
        msg.setOpaque(false);
        msg.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        
        String text = "I have " + product + " for Rp " + StyleUtil.formatRupiah(price);
        JLabel lbl = new JLabel("<html><b>" + seller + ":</b> " + text + "</html>");
        lbl.setForeground(Color.WHITE);
        
        JButton btnAdd = new JButton("🛒 Add");
        StyleUtil.styleActionButton(btnAdd, StyleUtil.SUCCESS);
        btnAdd.setMargin(new Insets(2, 8, 2, 8));
        btnAdd.setFont(StyleUtil.FONT_SMALL);
        
        btnAdd.addActionListener(e -> {
            Seller s = new Seller(seller, "", "", 5.0);
            Product p = new Product("P-TEMP", product, 1000, (int)price);
            Offer offer = new Offer(s, p, price, quantity <= 0 ? 1 : quantity, 5.0, "", null);
            selectedOffers.add(offer);
            refreshCartUI();
        });
        
        msg.add(lbl, BorderLayout.CENTER);
        msg.add(btnAdd, BorderLayout.EAST);
        
        msg.setMaximumSize(new Dimension(Integer.MAX_VALUE, msg.getPreferredSize().height + 10));
        
        chatContent.add(msg);
        chatContent.revalidate();
        scrollBottom();
    }

    private void addChatMessage(String sender, String text) {
        JPanel msg = new JPanel(new BorderLayout());
        msg.setOpaque(false);
        msg.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        
        JLabel lbl = new JLabel("<html><b>" + sender + ":</b> " + text + "</html>");
        lbl.setForeground(sender.equals("YOU") ? StyleUtil.GOLD_LIGHT : Color.WHITE);
        
        msg.add(lbl, BorderLayout.WEST);
        
        msg.setMaximumSize(new Dimension(Integer.MAX_VALUE, msg.getPreferredSize().height + 10));
        
        chatContent.add(msg);
        chatContent.revalidate();
        scrollBottom();
    }

    private void handleCheckout() {
        if (selectedOffers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }
        Payment p = TenderController.getInstance().checkout(selectedOffers, addressInput.getText());
        JOptionPane.showMessageDialog(this, "Checkout Successful! ID: " + p.getId());
        selectedOffers.clear();
        refreshCartUI();
    }

    private void refreshCartUI() {
        cartItemsPanel.removeAll();
        double total = 0;
        for (Offer o : selectedOffers) {
            total += o.getTotalPrice();
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            row.add(new JLabel("• " + o.getProduct().getName()), BorderLayout.CENTER);
            
            JButton btnRemove = new JButton("❌");
            btnRemove.setContentAreaFilled(false);
            btnRemove.setBorderPainted(false);
            btnRemove.setForeground(Color.RED);
            btnRemove.setMargin(new Insets(0, 2, 0, 2));
            btnRemove.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRemove.addActionListener(e -> {
                selectedOffers.remove(o);
                refreshCartUI();
            });
            row.add(btnRemove, BorderLayout.EAST);

            cartItemsPanel.add(row);
        }
        totalLabel.setText("Total: Rp " + StyleUtil.formatRupiah(total));
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private void scrollBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = chatScroll.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    @Override public void onNewRequest(TenderRequest r) {}
    @Override public void onNewOffer(String requestId, Offer offer) {
        addChatMessage("SYSTEM", "New offer received for: " + offer.getProduct().getName());
    }
}