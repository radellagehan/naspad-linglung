package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BuyerPanel extends JPanel implements TenderController.TenderListener {

    private JPanel chatContent;
    private JScrollPane chatScroll;
    private JTextField requestInput;
    private JTextField addressInput;
    private JButton sendButton;

    private JPanel cartItemsPanel;
    private JLabel totalLabel;
    private JButton checkoutButton;
    private final List<Offer> selectedOffers = new ArrayList<>();
    private final Map<String, JPanel> offerContainers = new HashMap<>();

    public BuyerPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(StyleUtil.BG_DARK);
        TenderController.getInstance().addListener(this);

        chatContent = new JPanel();
        chatContent.setLayout(new BoxLayout(chatContent, BoxLayout.Y_AXIS));
        chatContent.setBackground(StyleUtil.BG_DARK);
        chatContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatScroll = new JScrollPane(chatContent);
        chatScroll.setBorder(null);
        chatScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatScroll.getViewport().setBackground(StyleUtil.BG_DARK);
        chatScroll.setBackground(StyleUtil.BG_DARK);

        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setBackground(StyleUtil.BG_CARD);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.GOLD_DARK));
        bottom.add(buildCartSection(), BorderLayout.CENTER);
        bottom.add(buildInputSection(), BorderLayout.SOUTH);

        add(chatScroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        updateCartDisplay();
    }

    private JPanel buildCartSection() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(StyleUtil.BG_CARD);
        outer.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));

        JLabel cartTitle = new JLabel(StyleUtil.ICON_CART + "  Your Cart");
        cartTitle.setFont(StyleUtil.FONT_BOLD);
        cartTitle.setForeground(StyleUtil.GOLD_PRIMARY);

        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(StyleUtil.BG_CARD);

        JScrollPane cartScroll = new JScrollPane(cartItemsPanel);
        cartScroll.setBorder(null);
        cartScroll.setBackground(StyleUtil.BG_CARD);
        cartScroll.getViewport().setBackground(StyleUtil.BG_CARD);
        cartScroll.setPreferredSize(new Dimension(0, 90));
        cartScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel addrRow = new JPanel(new BorderLayout(8, 0));
        addrRow.setOpaque(false);
        addrRow.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        JLabel addrIcon = new JLabel("📍");
        addrIcon.setFont(StyleUtil.FONT_BODY);
        addressInput = new JTextField(TenderController.getInstance().getCurrentUser().getAddress());
        addressInput.setFont(StyleUtil.FONT_BODY);
        addressInput.setBackground(StyleUtil.BG_SURFACE);
        addressInput.setForeground(StyleUtil.TEXT_PRIMARY);
        addressInput.setCaretColor(StyleUtil.GOLD_PRIMARY);
        addressInput.putClientProperty("JTextField.placeholderText", "Enter delivery address...");
        addrRow.add(addrIcon, BorderLayout.WEST);
        addrRow.add(addressInput, BorderLayout.CENTER);

        JPanel checkoutRow = new JPanel(new BorderLayout(10, 0));
        checkoutRow.setOpaque(false);
        checkoutRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        totalLabel = new JLabel("Total: Rp 0");
        totalLabel.setFont(StyleUtil.FONT_HEADER);
        totalLabel.setForeground(StyleUtil.GOLD_LIGHT);
        checkoutButton = new JButton("Pay Now 💳");
        StyleUtil.styleActionButton(checkoutButton, StyleUtil.GOLD_PRIMARY);
        checkoutButton.addActionListener(e -> performCheckout());
        checkoutRow.add(totalLabel, BorderLayout.CENTER);
        checkoutRow.add(checkoutButton, BorderLayout.EAST);

        JPanel bottomCart = new JPanel(new BorderLayout());
        bottomCart.setOpaque(false);
        bottomCart.add(addrRow, BorderLayout.NORTH);
        bottomCart.add(checkoutRow, BorderLayout.SOUTH);

        outer.add(cartTitle, BorderLayout.NORTH);
        outer.add(cartScroll, BorderLayout.CENTER);
        outer.add(bottomCart, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildInputSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(StyleUtil.BG_ELEVATED);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.GOLD_DARK),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        requestInput = new JTextField();
        requestInput.setFont(StyleUtil.FONT_BODY);
        requestInput.setBackground(StyleUtil.BG_SURFACE);
        requestInput.setForeground(StyleUtil.TEXT_PRIMARY);
        requestInput.setCaretColor(StyleUtil.GOLD_PRIMARY);
        requestInput.putClientProperty("JTextField.placeholderText", "Type your order (e.g: chicken rice 2 iced tea 3)...");
        requestInput.addActionListener(e -> sendRequest());
        sendButton = new JButton(StyleUtil.ICON_SEND + "  Post Request");
        StyleUtil.styleActionButton(sendButton, StyleUtil.GOLD_PRIMARY);
        sendButton.addActionListener(e -> sendRequest());
        panel.add(requestInput, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        return panel;
    }

    private void sendRequest() {
        String text = requestInput.getText().trim();
        if (text.isEmpty()) return;
        addBubbleUser(text);
        Map<String, Integer> parsed = TenderController.getInstance().parseOrderText(text);
        if (!parsed.isEmpty()) {
            StringBuilder sb = new StringBuilder("<b>Parsed Order:</b><br>");
            parsed.forEach((k, v) -> sb.append("◆ ").append(k).append(" &nbsp;×").append(v).append("<br>"));
            addBubbleSystem(sb.toString());
        }
        TenderController.getInstance().postRequest(text, "", addressInput.getText());
        requestInput.setText("");
    }

    private void performCheckout() {
        if (selectedOffers.isEmpty()) { showMsg("Your cart is empty!"); return; }
        String addr = addressInput.getText().trim();
        if (addr.isEmpty()) { showMsg("Please enter a delivery address!"); return; }
        Payment payment = TenderController.getInstance().checkout(selectedOffers, addr);
        if (payment != null) {
            String html = "<b>✦ Payment Successful!</b><br>"
                + "Order ID: <b>#" + payment.getId() + "</b><br>"
                + "Total: <b>Rp " + StyleUtil.formatRupiah(payment.getTotalAmount()) + "</b><br>"
                + "Delivering to: " + payment.getBuyerAddress();
            addBubbleSystem(html);
            selectedOffers.clear();
            updateCartDisplay();
            JOptionPane.showMessageDialog(this, "Payment complete! Order #" + payment.getId(), "MinangLuxe", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addBubbleUser(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(StyleUtil.BG_DARK);
        JLabel bubble = new JLabel("<html><div style='width:260px;padding:10px 14px;background:#1a1060;border-radius:12px;color:#e0d0ff;font-size:13px;'>" + text + "</div></html>");
        bubble.setFont(StyleUtil.FONT_BODY);
        row.add(bubble);
        chatContent.add(row);
        chatContent.add(Box.createVerticalStrut(8));
        chatContent.revalidate(); chatContent.repaint();
        scrollBottom();
    }

    private void addBubbleSystem(String html) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setBackground(StyleUtil.BG_DARK);
        JLabel bubble = new JLabel("<html><div style='width:260px;padding:10px 14px;background:#2a0a30;border:1px solid #8a2060;border-radius:12px;color:#ffb0d8;font-size:12px;'>" + html + "</div></html>");
        bubble.setFont(StyleUtil.FONT_SMALL);
        row.add(bubble);
        chatContent.add(row);
        chatContent.add(Box.createVerticalStrut(8));
        chatContent.revalidate(); chatContent.repaint();
        scrollBottom();
    }

    @Override
    public void onNewRequest(TenderRequest request) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        container.setBackground(StyleUtil.BG_DARK);
        JPanel innerBox = new JPanel();
        innerBox.setLayout(new BoxLayout(innerBox, BoxLayout.Y_AXIS));
        innerBox.setBackground(StyleUtil.BG_DARK);
        JLabel waiting = new JLabel("<html><span style='color:#8090c0;font-size:11px;'>Searching for best offers... 🔍</span></html>");
        waiting.setFont(StyleUtil.FONT_SMALL);
        innerBox.add(waiting);
        container.add(innerBox);
        offerContainers.put(request.getRequestId(), innerBox);
        chatContent.add(container);
        chatContent.add(Box.createVerticalStrut(8));
        chatContent.revalidate(); chatContent.repaint();
        scrollBottom();
    }

    @Override
    public void onNewOffer(String requestId, Offer offer) {
        JPanel c = offerContainers.get(requestId);
        if (c != null) {
            c.removeAll();
            JLabel info = new JLabel("<html><div style='width:240px;font-size:11px;color:#aaa;'>Offers received! Check the <b>Best Recommendations</b> panel to choose your package.</div></html>");
            info.setFont(StyleUtil.FONT_SMALL);
            c.add(info);
            c.revalidate(); c.repaint();
            scrollBottom();
        }
    }

    public void addOfferToCart(Offer offer) {
        selectedOffers.add(offer);
        updateCartDisplay();
    }

    private void removeOfferFromCart(Offer offer) {
        selectedOffers.remove(offer);
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartItemsPanel.removeAll();
        double total = 0;
        if (selectedOffers.isEmpty()) {
            JLabel empty = new JLabel("Cart is empty");
            empty.setFont(StyleUtil.FONT_SMALL);
            empty.setForeground(StyleUtil.TEXT_MUTED);
            cartItemsPanel.add(empty);
            checkoutButton.setEnabled(false);
        } else {
            checkoutButton.setEnabled(true);
            for (Offer o : selectedOffers) {
                total += o.getTotalPrice();
                JPanel row = new JPanel(new BorderLayout(6, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BG_ELEVATED),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
                JLabel name = new JLabel("◆ " + o.getProduct().getName() + " x" + o.getQuantity());
                name.setFont(StyleUtil.FONT_SMALL);
                name.setForeground(StyleUtil.TEXT_PRIMARY);
                JLabel price = new JLabel("Rp " + StyleUtil.formatRupiah(o.getTotalPrice()));
                price.setFont(StyleUtil.FONT_LABEL);
                price.setForeground(StyleUtil.GOLD_ACCENT);
                JButton del = new JButton("✕");
                del.setFont(new Font("SansSerif", Font.BOLD, 11));
                del.setForeground(Color.WHITE);
                del.setBackground(StyleUtil.DANGER);
                del.setBorderPainted(false);
                del.setFocusPainted(false);
                del.setOpaque(true);
                del.setPreferredSize(new Dimension(22, 22));
                del.setCursor(new Cursor(Cursor.HAND_CURSOR));
                del.addActionListener(e -> removeOfferFromCart(o));
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
                right.setOpaque(false);
                right.add(price); right.add(del);
                row.add(name, BorderLayout.CENTER);
                row.add(right, BorderLayout.EAST);
                cartItemsPanel.add(row);
            }
        }
        totalLabel.setText("Total: Rp " + StyleUtil.formatRupiah(total));
        cartItemsPanel.revalidate(); cartItemsPanel.repaint();
    }

    private void scrollBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = chatScroll.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, "MinangLuxe", JOptionPane.WARNING_MESSAGE);
    }
}
