package com.minangluxe.tender.view;

import com.minangluxe.tender.model.Seller;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SellerContactDialog extends JDialog {

    private final Seller seller;
    private JLabel mapLabel;

    public SellerContactDialog(Frame owner, Seller seller) {
        super(owner, "Seller Info — " + seller.getName(), true);
        this.seller = seller;
        setLayout(new BorderLayout(12, 12));
        setSize(720, 540);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(StyleUtil.BG_DARK);
        setResizable(true);
        buildUI();
    }

    public static void showFor(Component parent, Seller seller) {
        Frame frame = parent instanceof Frame f ? f : (Frame) SwingUtilities.getWindowAncestor(parent);
        new SellerContactDialog(frame, seller).setVisible(true);
    }

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(StyleUtil.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("♦  Seller Info & Location");
        title.setFont(StyleUtil.FONT_TITLE);
        title.setForeground(StyleUtil.GOLD_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(title);
        main.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Contact the seller for custom orders or stock inquiries.");
        sub.setFont(StyleUtil.FONT_SMALL);
        sub.setForeground(StyleUtil.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(sub);
        main.add(Box.createVerticalStrut(16));

        JPanel infoCard = new JPanel(new GridLayout(0, 1, 6, 6));
        infoCard.setBackground(StyleUtil.BG_CARD);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        addInfoRow(infoCard, "Shop Name",  seller.getName());
        addInfoRow(infoCard, "Seller ID",  seller.getId().length() > 12 ? seller.getId().substring(0, 12) + "…" : seller.getId());
        addInfoRow(infoCard, "Phone / WA", seller.getContactId().isEmpty() ? "—" : seller.getContactId());
        addInfoRow(infoCard, "Address",    seller.getAddress().isEmpty()   ? "—" : seller.getAddress());
        main.add(infoCard);
        main.add(Box.createVerticalStrut(20));

        JLabel mapTitle = new JLabel("Location Map");
        mapTitle.setFont(StyleUtil.FONT_BOLD);
        mapTitle.setForeground(StyleUtil.TEXT_PRIMARY);
        mapTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(mapTitle);
        main.add(Box.createVerticalStrut(8));

        mapLabel = new JLabel("Loading map...", SwingConstants.CENTER);
        mapLabel.setOpaque(true);
        mapLabel.setBackground(StyleUtil.BG_CARD);
        mapLabel.setForeground(StyleUtil.TEXT_MUTED);
        mapLabel.setFont(StyleUtil.FONT_SMALL);
        mapLabel.setBorder(BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true));
        mapLabel.setPreferredSize(new Dimension(460, 260));
        mapLabel.setMaximumSize(new Dimension(Short.MAX_VALUE, 260));
        mapLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(mapLabel);
        main.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton openMapBtn = new JButton("Open in Maps");
        StyleUtil.styleActionButton(openMapBtn, StyleUtil.GOLD_PRIMARY);
        openMapBtn.addActionListener(e -> openMaps());
        btnRow.add(openMapBtn);
        if (!seller.getContactId().isEmpty()) {
            JButton waBtn = new JButton("WhatsApp Chat");
            StyleUtil.styleActionButton(waBtn, new Color(37, 211, 102));
            waBtn.addActionListener(e -> openWhatsApp());
            btnRow.add(waBtn);
        }
        main.add(btnRow);
        main.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(null);
        scroll.setBackground(StyleUtil.BG_DARK);
        scroll.getViewport().setBackground(StyleUtil.BG_DARK);
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(StyleUtil.BG_CARD);
        south.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.GOLD_DARK));
        JButton closeBtn = new JButton("Close");
        StyleUtil.styleActionButton(closeBtn, StyleUtil.BG_ELEVATED);
        closeBtn.setForeground(StyleUtil.TEXT_PRIMARY);
        closeBtn.addActionListener(e -> setVisible(false));
        south.add(closeBtn);
        add(south, BorderLayout.SOUTH);

        loadMapPreview();
    }

    private void addInfoRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel l = new JLabel(label + ":");
        l.setFont(StyleUtil.FONT_LABEL);
        l.setForeground(StyleUtil.TEXT_MUTED);
        l.setPreferredSize(new Dimension(100, 20));
        JLabel v = new JLabel(value);
        v.setFont(StyleUtil.FONT_BODY);
        v.setForeground(StyleUtil.TEXT_PRIMARY);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        parent.add(row);
    }

    private void loadMapPreview() {
        String loc = seller.getAddress();
        if (loc == null || loc.isBlank()) loc = "Jakarta, Indonesia";
        final String query = loc.trim();
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override protected ImageIcon doInBackground() {
                try {
                    String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                    String urlStr = "https://staticmap.openstreetmap.de/staticmap.php?center=" + encoded + "&zoom=15&size=460x260&maptype=mapnik&markers=" + encoded + ",red-pushpin";
                    ImageIcon icon = new ImageIcon(new URL(urlStr));
                    return icon.getIconWidth() > 0 ? icon : null;
                } catch (Exception ex) { return null; }
            }
            @Override protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) { mapLabel.setText(null); mapLabel.setIcon(icon); }
                    else mapLabel.setText("Map preview not available.");
                } catch (Exception ignored) { mapLabel.setText("Map preview not available."); }
            }
        };
        worker.execute();
    }

    private void openMaps() {
        String loc = seller.getAddress();
        if (loc == null || loc.isBlank()) loc = "Indonesia";
        try {
            URI uri = URI.create("https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(loc.trim(), StandardCharsets.UTF_8));
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) Desktop.getDesktop().browse(uri);
            else JOptionPane.showMessageDialog(this, "Open this URL:\n" + uri);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Could not open maps: " + ex.getMessage()); }
    }

    private void openWhatsApp() {
        String phone = seller.getContactId().replaceAll("[^0-9]", "");
        if (phone.startsWith("0")) phone = "62" + phone.substring(1);
        else if (!phone.startsWith("62")) phone = "62" + phone;
        try {
            URI uri = URI.create("https://wa.me/" + phone);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) Desktop.getDesktop().browse(uri);
            else JOptionPane.showMessageDialog(this, "Open in browser:\n" + uri);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Could not open WhatsApp: " + ex.getMessage()); }
    }
}
