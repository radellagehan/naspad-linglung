package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OrderHistoryPanel extends JPanel implements TenderController.TenderListener {

    private JPanel listPanel;

    public OrderHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleUtil.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        TenderController.getInstance().addListener(this);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, StyleUtil.GOLD_DARK),
            BorderFactory.createEmptyBorder(0, 0, 16, 0)
        ));
        JLabel title = new JLabel("📜  Order History");
        title.setFont(StyleUtil.FONT_TITLE);
        title.setForeground(StyleUtil.GOLD_PRIMARY);
        JLabel sub = new JLabel("All your completed transactions");
        sub.setFont(StyleUtil.FONT_SMALL);
        sub.setForeground(StyleUtil.TEXT_MUTED);
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(title);
        titleStack.add(sub);
        topBar.add(titleStack, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(StyleUtil.BG_DARK);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setBackground(StyleUtil.BG_DARK);
        scroll.getViewport().setBackground(StyleUtil.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        refreshList();
    }

    private void refreshList() {
        listPanel.removeAll();
        List<Payment> history = TenderController.getInstance().getPaymentHistory();
        if (history.isEmpty()) {
            listPanel.add(Box.createVerticalStrut(60));
            JLabel empty = new JLabel("No orders yet. Start shopping! 🍜");
            empty.setFont(StyleUtil.FONT_BODY);
            empty.setForeground(StyleUtil.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(empty);
        } else {
            listPanel.add(Box.createVerticalStrut(12));
            for (Payment p : history) {
                listPanel.add(buildCard(p));
                listPanel.add(Box.createVerticalStrut(14));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildCard(Payment p) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(StyleUtil.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel orderId = new JLabel("Order #" + p.getId());
        orderId.setFont(StyleUtil.FONT_BOLD);
        orderId.setForeground(StyleUtil.GOLD_LIGHT);
        JLabel dateLabel = new JLabel(p.getFormattedTimestamp());
        dateLabel.setFont(StyleUtil.FONT_SMALL);
        dateLabel.setForeground(StyleUtil.TEXT_MUTED);
        JPanel leftInfo = new JPanel(new GridLayout(2, 1));
        leftInfo.setOpaque(false);
        leftInfo.add(orderId);
        leftInfo.add(dateLabel);
        JLabel totalLabel = new JLabel("Rp " + StyleUtil.formatRupiah(p.getTotalAmount()));
        totalLabel.setFont(StyleUtil.FONT_HEADER);
        totalLabel.setForeground(StyleUtil.SUCCESS);
        topRow.add(leftInfo,   BorderLayout.WEST);
        topRow.add(totalLabel, BorderLayout.EAST);

        JSeparator sep = StyleUtil.goldSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel itemsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        itemsPanel.setOpaque(false);
        for (Offer item : p.getItems()) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            JLabel itemLbl = new JLabel("◆ " + item.getProduct().getName() + " x" + item.getQuantity() + " — " + item.getSeller().getName());
            itemLbl.setFont(StyleUtil.FONT_BODY);
            itemLbl.setForeground(StyleUtil.TEXT_PRIMARY);
            JButton contactBtn = new JButton("Contact Seller");
            StyleUtil.styleLinkButton(contactBtn);
            contactBtn.addActionListener(e -> SellerContactDialog.showFor(this, item.getSeller()));
            row.add(itemLbl,    BorderLayout.CENTER);
            row.add(contactBtn, BorderLayout.EAST);
            itemsPanel.add(row);
        }

        JLabel addrLabel = new JLabel("📍 Delivered to: " + p.getBuyerAddress());
        addrLabel.setFont(StyleUtil.FONT_SMALL);
        addrLabel.setForeground(StyleUtil.TEXT_MUTED);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(sep);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(itemsPanel);

        card.add(topRow,      BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(addrLabel,   BorderLayout.SOUTH);
        return card;
    }

    @Override public void onPaymentCompleted(Payment payment) { refreshList(); }
    @Override public void onNewRequest(TenderRequest r) {}
    @Override public void onNewOffer(String id, Offer o) {}
}
