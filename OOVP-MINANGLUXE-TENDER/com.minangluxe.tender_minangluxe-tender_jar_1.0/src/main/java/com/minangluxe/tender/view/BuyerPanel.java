package com.minangluxe.tender.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.CartItem;
import com.minangluxe.tender.model.Offer;
import com.minangluxe.tender.model.Payment;
import com.minangluxe.tender.model.Product;
import com.minangluxe.tender.model.Seller;
import com.minangluxe.tender.model.TenderRequest;
import com.minangluxe.tender.model.TenderRequirement;
import com.minangluxe.tender.util.DatabaseConnection;
import com.minangluxe.tender.util.OrderParser;
import com.minangluxe.tender.util.RequirementValidator;
import com.minangluxe.tender.util.StyleUtil;

public class BuyerPanel extends JPanel implements TenderController.TenderListener {

    private TenderRequirement currentRequirement; // parsed from latest buyer request
    private String selectedMenu = null; // A/B/C after buyer confirmation


    private JPanel chatContent;
    private JScrollPane chatScroll;
    private JTextField requestInput;
    private JTextField addressInput;
    private JButton sendButton;



    private JPanel cartItemsPanel;
    private JLabel totalLabel;
    private JButton checkoutButton;
    private final List<CartItem> selectedOffers = new ArrayList<>();
    private final List<Offer> selectedOfferCheckout = new ArrayList<>();

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
        actions.add(sendButton);


        inputPanel.add(fields, BorderLayout.CENTER);
        inputPanel.add(actions, BorderLayout.SOUTH);

        // --- SIDEBAR KERANJANG (KANAN) ---
        JPanel sidebar = createCartSidebar();

        add(chatScroll, BorderLayout.CENTER);
        add(sidebar, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // --- LOGIC INTERACTION ---

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

        // Build a lightweight offer for validation.
        Seller s = new Seller(seller, "", "", 5.0);
        Product p = new Product("P-TEMP", product, 1000, (int) price);
        Offer offerPreview = new Offer(s, p, price, quantity <= 0 ? 1 : quantity, 5.0, "", null);


        RequirementValidator.ValidationResult validation = RequirementValidator.validate(currentRequirement, offerPreview);


        // If invalid, disable Add.
        boolean canAdd = validation.accepted;
        String reason = validation.reason;

        // Menu ABC mapping: untuk sekarang belum ada mapping penuh need→solusi.
        // Agar UI tidak “ngaco”, Menu hanya diperlukan untuk buyer konfirmasi (no extra filtering).
        // Validasi ketat tetap dilakukan oleh RequirementValidator (budget/quantity).


        JButton btnAdd = new JButton("🛒 Add");

        StyleUtil.styleActionButton(btnAdd, StyleUtil.SUCCESS);
        btnAdd.setMargin(new Insets(2, 8, 2, 8));
        btnAdd.setFont(StyleUtil.FONT_SMALL);
        btnAdd.setEnabled(canAdd);
        if (!canAdd && reason != null && !reason.isBlank()) {
            btnAdd.setToolTipText("Offer not match requirement: " + reason);
        }

        // For MVP: we create CartItem only after we know offerId/requestId from DB.
        // Since current polling only passes offer fields (not IDs), we temporarily store with dummy IDs.
        // We'll upgrade loadOffersFromDB to include ids in a subsequent edit.
        btnAdd.addActionListener(e -> {
            Seller s2 = new Seller(seller, "", "", 5.0);
            Product p2 = new Product("P-TEMP", product, 1000, (int) price);
            Offer offer = new Offer(s2, p2, price, quantity <= 0 ? 1 : quantity, 5.0, "", null);
            // dummy ids until loadOffersFromDB selects them
            selectedOffers.add(new CartItem(-1, -1, seller, s2, p2, price, quantity <= 0 ? 1 : quantity, 5.0));
            refreshCartUI();
        });


        JPanel msg = new JPanel(new BorderLayout(10, 0));
        msg.setOpaque(false);
        msg.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        
        String text = "I have " + product + " for Rp " + StyleUtil.formatRupiah(price);
        JLabel lbl = new JLabel("<html><b>" + seller + ":</b> " + text + "</html>");
        lbl.setForeground(Color.WHITE);
        
        // msg already uses the validated btnAdd above.
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
        // checkout tetap memakai Offer model yang ada
        selectedOfferCheckout.clear();
        TenderRequest requestRef = new TenderRequest(TenderController.getInstance().getCurrentUser(), "", "", addressInput.getText());
        for (CartItem ci : selectedOffers) {
            selectedOfferCheckout.add(ci.toOffer(requestRef));
        }
        Payment p = TenderController.getInstance().checkout(selectedOfferCheckout, addressInput.getText());
        JOptionPane.showMessageDialog(this, "Checkout Successful! ID: " + p.getId());
        selectedOffers.clear();
        refreshCartUI();
    }

    private void refreshCartUI() {
        cartItemsPanel.removeAll();
        double total = 0;
        for (CartItem o : selectedOffers) {
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

    @Override public void onNewRequest(TenderRequest r) {
        // Parse requirement from buyer's raw query string.
        // For now we only use qtyTotal & budgetPerItem/budgetTotal; other fields are optional.
        java.util.Map<String, String> parsed = OrderParser.parseToMap(r.getQuery());

        // Reset menu confirmation state for the new request.
        selectedMenu = null;


        String paket = parsed.getOrDefault("paket", null);
        Integer berat = null;
        if (parsed.containsKey("berat")) {
            try {
                berat = Integer.parseInt(parsed.get("berat"));
            } catch (Exception ignored) {}
        }

        Integer qtyTotal = null;
        if (parsed.containsKey("qtyTotal")) {
            try {
                qtyTotal = Integer.parseInt(parsed.get("qtyTotal"));
            } catch (Exception ignored) {}
        }

        Integer qtyHot = null;
        if (parsed.containsKey("qtyHot")) {
            try {
                qtyHot = Integer.parseInt(parsed.get("qtyHot"));
            } catch (Exception ignored) {}
        }

        Long budgetPerItem = null;
        if (parsed.containsKey("budgetPerItem")) {
            try {
                budgetPerItem = Long.parseLong(parsed.get("budgetPerItem"));
            } catch (Exception ignored) {}
        }

        Long budgetTotal = null;
        if (parsed.containsKey("budgetTotal")) {
            try {
                budgetTotal = Long.parseLong(parsed.get("budgetTotal"));
            } catch (Exception ignored) {}
        }

        String need = parsed.getOrDefault("need", null);
        currentRequirement = new TenderRequirement(paket, berat, need, qtyTotal, qtyHot, budgetTotal, budgetPerItem);

        // --- UI: Menu A/B/C + Konfirmasi (no extra filtering yet; validation happens via RequirementValidator) ---
        SwingUtilities.invokeLater(() -> {

            JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            menuPanel.setOpaque(false);
            menuPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));


            JLabel title = new JLabel("🧾 Select menu (A/B/C) then Konfirmasi:");
            title.setForeground(Color.WHITE);
            title.setFont(StyleUtil.FONT_SMALL);

            JButton btnA = new JButton("Menu A");
            JButton btnB = new JButton("Menu B");
            JButton btnC = new JButton("Menu C");
            StyleUtil.styleActionButton(btnA, StyleUtil.GOLD_PRIMARY);
            StyleUtil.styleActionButton(btnB, StyleUtil.GOLD_PRIMARY);
            StyleUtil.styleActionButton(btnC, StyleUtil.GOLD_PRIMARY);

            // A: termurah unit price. B/C: untuk sementara ikut A.
            btnA.addActionListener(e -> {
                selectedMenu = "A";
                btnA.setBackground(StyleUtil.SUCCESS);
                btnB.setBackground(StyleUtil.GOLD_PRIMARY);
                btnC.setBackground(StyleUtil.GOLD_PRIMARY);
            });
            btnB.addActionListener(e -> {
                selectedMenu = "B";
                btnA.setBackground(StyleUtil.GOLD_PRIMARY);
                btnB.setBackground(StyleUtil.SUCCESS);
                btnC.setBackground(StyleUtil.GOLD_PRIMARY);
            });
            btnC.addActionListener(e -> {
                selectedMenu = "C";
                btnA.setBackground(StyleUtil.GOLD_PRIMARY);
                btnB.setBackground(StyleUtil.GOLD_PRIMARY);
                btnC.setBackground(StyleUtil.SUCCESS);
            });

            JButton btnKonfirmasi = new JButton("✅ Konfirmasi");
            StyleUtil.styleActionButton(btnKonfirmasi, StyleUtil.SUCCESS);
            btnKonfirmasi.addActionListener(e -> {
                if (selectedMenu == null) {
                    JOptionPane.showMessageDialog(this, "Pilih dulu Menu A/B/C sebelum konfirmasi.");
                    return;
                }
                // Saat ini menu hanya disimpan sebagai konteks; penyaringan offer masih via RequirementValidator.
                addChatMessage("SYSTEM", "Menu " + selectedMenu + " dikonfirmasi. (Penyaringan saat ini berbasis budget & quantity.)");
            });


            menuPanel.add(title);
            menuPanel.add(btnA);
            menuPanel.add(btnB);
            menuPanel.add(btnC);
            menuPanel.add(btnKonfirmasi);

            chatContent.add(menuPanel);
            chatContent.revalidate();
            scrollBottom();
        });
    }







    @Override public void onNewOffer(String requestId, Offer offer) {
        addChatMessage("SYSTEM", "New offer received for: " + offer.getProduct().getName());
    }
}