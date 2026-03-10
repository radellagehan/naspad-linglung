package com.minangluxe.tender.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class StyleUtil {
    // MinangLuxe Pink & Navy Palette
    public static final Color BG_DARK      = new Color(10, 15, 40);
    public static final Color BG_CARD      = new Color(16, 24, 58);
    public static final Color BG_SURFACE   = new Color(22, 33, 75);
    public static final Color BG_ELEVATED  = new Color(30, 45, 95);

    // Pink Accent Palette
    public static final Color GOLD_PRIMARY  = new Color(255, 105, 180);
    public static final Color GOLD_LIGHT    = new Color(255, 160, 210);
    public static final Color GOLD_DARK     = new Color(180, 60, 120);
    public static final Color GOLD_ACCENT   = new Color(255, 130, 195);

    // Semantic Colors
    public static final Color SUCCESS      = new Color(80, 220, 160);
    public static final Color DANGER       = new Color(255, 80, 100);
    public static final Color INFO         = new Color(100, 180, 255);
    public static final Color WARNING      = new Color(255, 200, 80);

    // Text Colors
    public static final Color TEXT_PRIMARY  = new Color(230, 235, 255);
    public static final Color TEXT_MUTED    = new Color(130, 145, 185);
    public static final Color TEXT_DARK     = new Color(10, 15, 40);

    // Fonts
    public static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_BODY   = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BOLD   = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_LABEL  = new Font("SansSerif", Font.BOLD, 12);

    // Icons (unicode)
    public static final String ICON_BUYER   = "👑";
    public static final String ICON_SELLER  = "🏪";
    public static final String ICON_CART    = "🛒";
    public static final String ICON_SEND    = "✦";
    public static final String ICON_STAR    = "★";

    public static Border cardBorder(Color accent) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 1, true),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        );
    }

    public static Border goldBorder() {
        return cardBorder(GOLD_PRIMARY);
    }

    public static void styleHeader(JLabel label) {
        label.setFont(FONT_HEADER);
        label.setForeground(GOLD_PRIMARY);
    }

    public static void styleActionButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(TEXT_DARK);
        btn.setFont(FONT_BOLD);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1, true),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
    }

    public static void styleLinkButton(JButton btn) {
        btn.setFont(FONT_SMALL);
        btn.setForeground(INFO);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
    }

    public static Color getSellerColor(String sellerName) {
        int hash = Math.abs(sellerName.hashCode());
        Color[] colors = {
            new Color(50, 60, 80),
            new Color(55, 50, 80),
            new Color(40, 65, 75),
            new Color(65, 50, 55),
            new Color(45, 65, 55),
            new Color(60, 55, 45)
        };
        return colors[hash % colors.length];
    }

    public static String formatRupiah(double amount) {
        java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(java.util.Locale.GERMANY);
        return fmt.format(amount);
    }

    /** Thin gold separator */
    public static JSeparator goldSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(GOLD_DARK);
        sep.setBackground(GOLD_DARK);
        return sep;
    }
}
