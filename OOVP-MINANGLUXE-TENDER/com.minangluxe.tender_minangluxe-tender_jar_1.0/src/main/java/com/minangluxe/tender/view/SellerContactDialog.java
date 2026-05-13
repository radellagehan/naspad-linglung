package com.minangluxe.tender.view;

import com.minangluxe.tender.model.Seller;
import com.minangluxe.tender.util.StyleUtil;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SellerContactDialog extends JDialog {
    private final Seller seller;
    private JLabel mapLabel;
    private JTextField phoneField;
    private JTextField addressField;
    private Runnable onSaveCallback;

    public SellerContactDialog(Frame owner, Seller seller, Runnable onSaveCallback) {
        super(owner, "Store Profile — " + seller.getId(), true);
        this.seller = seller;
        this.onSaveCallback = onSaveCallback;
        setLayout(new BorderLayout(12, 12));
        setSize(500, 650);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(StyleUtil.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(StyleUtil.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Edit Store Profile");
        title.setFont(StyleUtil.FONT_TITLE);
        title.setForeground(StyleUtil.GOLD_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(title);
        main.add(Box.createVerticalStrut(15));

        phoneField = addEditRow(main, "📞 Phone / WhatsApp:", seller.getPhoneNumber());
        addressField = addEditRow(main, "📍 Store Location / Map:", seller.getAddress());

        main.add(Box.createVerticalStrut(10));
        
        // WhatsApp Action
        JButton waBtn = new JButton("💬 Chat on WhatsApp");
        StyleUtil.styleActionButton(waBtn, StyleUtil.SUCCESS);
        waBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        waBtn.addActionListener(e -> openWhatsApp());
        main.add(waBtn);
        
        main.add(Box.createVerticalStrut(10));

        // Maps Preview
        mapLabel = new JLabel("Loading map preview...", SwingConstants.CENTER);
        mapLabel.setPreferredSize(new Dimension(440, 240));
        mapLabel.setMaximumSize(new Dimension(440, 240));
        mapLabel.setBorder(BorderFactory.createLineBorder(StyleUtil.GOLD_DARK));
        mapLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(mapLabel);

        main.add(Box.createVerticalStrut(10));

        JButton openMapBtn = new JButton("🗺️ Open in Google Maps");
        StyleUtil.styleActionButton(openMapBtn, StyleUtil.GOLD_PRIMARY);
        openMapBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        openMapBtn.addActionListener(e -> openMaps());
        main.add(openMapBtn);

        main.add(Box.createVerticalStrut(20));

        // Save Button
        JButton saveBtn = new JButton("Save Changes");
        StyleUtil.styleActionButton(saveBtn, new Color(70, 70, 150));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            seller.setPhoneNumber(phoneField.getText());
            seller.setAddress(addressField.getText());
            loadMapPreview();
            if (onSaveCallback != null) onSaveCallback.run();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
        });
        main.add(saveBtn);

        add(new JScrollPane(main), BorderLayout.CENTER);
        loadMapPreview();
    }

    private JTextField addEditRow(JPanel parent, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(StyleUtil.FONT_LABEL);
        lbl.setForeground(StyleUtil.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        
        JTextField tf = new JTextField(value);
        tf.setFont(StyleUtil.FONT_BODY);
        tf.setBackground(StyleUtil.BG_SURFACE);
        tf.setForeground(StyleUtil.TEXT_PRIMARY);
        tf.setCaretColor(StyleUtil.GOLD_PRIMARY);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        parent.add(tf);
        parent.add(Box.createVerticalStrut(10));
        return tf;
    }

    private void loadMapPreview() {
        String loc = addressField.getText(); 
        if (loc == null || loc.isBlank()) loc = "Jakarta";
        
        String finalLoc = loc;
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override protected ImageIcon doInBackground() {
                try {
                    String encoded = URLEncoder.encode(finalLoc, StandardCharsets.UTF_8);
                    String urlStr = "https://staticmap.openstreetmap.de/staticmap.php?center=" + encoded + "&zoom=14&size=440x240&maptype=mapnik";
                    return new ImageIcon(URI.create(urlStr).toURL());
                } catch (Exception ex) { return null; }
            }
            @Override protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) { mapLabel.setText(""); mapLabel.setIcon(icon); }
                    else { mapLabel.setText("Map preview not available"); mapLabel.setIcon(null); }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void openWhatsApp() {
        String phone = phoneField.getText().replaceAll("[^\\d]", "");
        if (phone.startsWith("0")) phone = "62" + phone.substring(1);
        try {
            Desktop.getDesktop().browse(URI.create("https://wa.me/" + phone));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open WhatsApp: " + ex.getMessage());
        }
    }

    private void openMaps() {
        String loc = addressField.getText(); 
        try {
            URI uri = URI.create("https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(loc, StandardCharsets.UTF_8));
            Desktop.getDesktop().browse(uri);
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Failed to open Maps: " + ex.getMessage());
        }
    }

    public static void showFor(Component parent, Seller seller, Runnable onSaveCallback) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window instanceof Frame) {
            new SellerContactDialog((Frame) window, seller, onSaveCallback).setVisible(true);
        } else {
            new SellerContactDialog(null, seller, onSaveCallback).setVisible(true);
        }
    }
}