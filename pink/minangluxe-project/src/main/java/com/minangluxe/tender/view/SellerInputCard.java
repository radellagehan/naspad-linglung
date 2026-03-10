package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;

public class SellerInputCard extends JPanel {
    private final Seller seller;
    private JTextField productField;
    private JTextField priceField;
    private JSpinner ratingSpinner;
    private JSpinner qtySpinner;
    private JButton submitBtn;
    private TenderRequest currentRequest;

    public SellerInputCard(Seller seller) {
        this.seller = seller;
        setLayout(new BorderLayout(6, 6));
        setBackground(StyleUtil.getSellerColor(seller.getName()));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Header
        JPanel header = new JPanel(new BorderLayout(6, 0));
        header.setOpaque(false);
        JLabel nameLabel = new JLabel(StyleUtil.ICON_SELLER + " " + seller.getName());
        nameLabel.setFont(StyleUtil.FONT_BOLD);
        nameLabel.setForeground(new Color(255, 160, 210));
        JButton infoBtn = new JButton("Info & Map");
        StyleUtil.styleLinkButton(infoBtn);
        infoBtn.addActionListener(e -> SellerContactDialog.showFor(this, seller));
        JLabel contactLbl = new JLabel("📞 " + (seller.getContactId().isEmpty() ? "—" : seller.getContactId()));
        contactLbl.setFont(StyleUtil.FONT_SMALL);
        contactLbl.setForeground(StyleUtil.TEXT_MUTED);
        JPanel headerTop = new JPanel(new BorderLayout());
        headerTop.setOpaque(false);
        headerTop.add(nameLabel, BorderLayout.WEST);
        headerTop.add(infoBtn,   BorderLayout.EAST);
        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.add(headerTop,  BorderLayout.NORTH);
        headerWrap.add(contactLbl, BorderLayout.SOUTH);
        header.add(headerWrap, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 6));
        form.setOpaque(false);
        form.add(makeLabel("Product:"));
        productField = makeTextField("Product name...");
        form.add(productField);
        form.add(makeLabel("Price (Rp):"));
        priceField = makeTextField("0");
        attachPriceFormatter(priceField);
        form.add(priceField);
        form.add(makeLabel("Rating:"));
        ratingSpinner = new JSpinner(new SpinnerNumberModel(4.5, 0.0, 5.0, 0.1));
        styleSpinner(ratingSpinner);
        form.add(ratingSpinner);
        form.add(makeLabel("Quantity:"));
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        styleSpinner(qtySpinner);
        form.add(qtySpinner);
        add(form, BorderLayout.CENTER);

        // Footer
        submitBtn = new JButton(StyleUtil.ICON_SEND + " Send Offer");
        StyleUtil.styleActionButton(submitBtn, StyleUtil.GOLD_PRIMARY);
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitOffer());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(submitBtn);
        add(footer, BorderLayout.SOUTH);
    }

    public void setRequest(TenderRequest req) {
        this.currentRequest = req;
        submitBtn.setEnabled(req != null);
        if (req == null) { productField.setText(""); priceField.setText(""); }
    }

    private void submitOffer() {
        if (currentRequest == null) { JOptionPane.showMessageDialog(this, "Please select a request first!"); return; }
        String name = productField.getText().trim();
        String priceStr = priceField.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Please fill in Product and Price!"); return; }
        try {
            double price = Double.parseDouble(priceStr.replaceAll("\\.", "").replaceAll(",", ""));
            int qty = (Integer) qtySpinner.getValue();
            double rating = (Double) ratingSpinner.getValue();
            Offer offer = new Offer(seller, new Product(name, name), price, qty, rating, "", currentRequest);
            TenderController.getInstance().submitOffer(currentRequest.getRequestId(), offer);
            submitBtn.setText("Sent ✓");
            submitBtn.setBackground(StyleUtil.SUCCESS);
            Timer t = new Timer(2000, e -> { submitBtn.setText(StyleUtil.ICON_SEND + " Send Offer"); StyleUtil.styleActionButton(submitBtn, StyleUtil.GOLD_PRIMARY); });
            t.setRepeats(false); t.start();
            productField.setText(""); priceField.setText(""); ratingSpinner.setValue(4.5);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price!");
        }
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(StyleUtil.FONT_LABEL);
        l.setForeground(StyleUtil.TEXT_MUTED);
        return l;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(StyleUtil.FONT_BODY);
        tf.setBackground(StyleUtil.BG_SURFACE);
        tf.setForeground(StyleUtil.TEXT_PRIMARY);
        tf.setCaretColor(StyleUtil.GOLD_PRIMARY);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(StyleUtil.FONT_BODY);
        sp.getEditor().getComponent(0).setBackground(StyleUtil.BG_SURFACE);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setForeground(StyleUtil.TEXT_PRIMARY);
    }

    private void attachPriceFormatter(JTextField tf) {
        tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { fmt(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { fmt(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { fmt(); }
            void fmt() {
                SwingUtilities.invokeLater(() -> {
                    String txt = tf.getText();
                    String clean = txt.replaceAll("[^\\d]", "");
                    if (clean.isEmpty()) return;
                    try {
                        long val = Long.parseLong(clean);
                        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(java.util.Locale.GERMANY);
                        String formatted = nf.format(val);
                        if (!txt.equals(formatted)) tf.setText(formatted);
                    } catch (NumberFormatException ignored) {}
                });
            }
        });
    }
}
