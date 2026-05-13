package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;

public class RecommendationPanel extends JPanel implements TenderController.TenderListener {

    private final BuyerPanel buyerPanel;
    private JPanel contentPanel;

    public BuyerPanel getBuyerPanel() { return buyerPanel; }

    public RecommendationPanel(BuyerPanel buyerPanel) {
        this.buyerPanel = buyerPanel;
        setLayout(new BorderLayout(8, 8));
        setBackground(StyleUtil.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Mendaftarkan panel ini ke Observer/Listener Controller
        TenderController.getInstance().addListener(this);

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("✦ Top Recommendations");
        title.setFont(StyleUtil.FONT_TITLE);
        title.setForeground(StyleUtil.GOLD_PRIMARY);

        JButton resetBtn = new JButton("↺ Refresh");
        StyleUtil.styleActionButton(resetBtn, StyleUtil.DANGER);
        resetBtn.addActionListener(e -> clearPanel());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(resetBtn, BorderLayout.EAST);

        // --- CONTENT PANEL ---
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(StyleUtil.BG_DARK);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(StyleUtil.BG_DARK);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        // Poll DB
        javax.swing.Timer pollTimer = new javax.swing.Timer(3000, e -> loadRecommendationsFromDB());
        pollTimer.start();
        loadRecommendationsFromDB();
    }

    private void clearPanel() {
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadRecommendationsFromDB() {
        new Thread(() -> {
            String sql = "SELECT id, seller_name, product_name, price " +
                         "FROM tender_offers ORDER BY price ASC LIMIT 10";
            java.sql.Connection conn = com.minangluxe.tender.util.DatabaseConnection.getConnection();
            java.util.List<JPanel> newCards = new java.util.ArrayList<>();
            
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    String seller = rs.getString("seller_name");
                    String product = rs.getString("product_name");
                    double price = rs.getDouble("price");
                    
                    JPanel card = new JPanel(new BorderLayout());
                    card.setBackground(StyleUtil.BG_CARD);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                    
                    String text = "<html><b>" + seller.toUpperCase() + "</b> offers <b>" + 
                                  product + "</b><br>" +
                                  "<font color='#FF69B4'>Price: Rp " + StyleUtil.formatRupiah(price) + "</font></html>";
                                  
                    JLabel lblOffer = new JLabel(text);
                    lblOffer.setForeground(Color.WHITE);
                    card.add(lblOffer, BorderLayout.CENTER);
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
                    newCards.add(card);
                }
            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            
            SwingUtilities.invokeLater(() -> {
                contentPanel.removeAll();
                for (JPanel c : newCards) {
                    contentPanel.add(c);
                    contentPanel.add(Box.createVerticalStrut(10));
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            });
            
        }).start();
    }

    // ========================================================
    // METHOD DARI TENDER LISTENER (Wajib ada biar gak error!)
    // ========================================================

    @Override 
    public void onNewRequest(TenderRequest request) {}

    @Override 
    public void onNewOffer(String requestId, Offer offer) {}
    
    @Override
    public void onPaymentCompleted(Payment payment) {
        // Optional: Reset rekomendasi kalau payment udah sukses
        clearPanel();
    }
}