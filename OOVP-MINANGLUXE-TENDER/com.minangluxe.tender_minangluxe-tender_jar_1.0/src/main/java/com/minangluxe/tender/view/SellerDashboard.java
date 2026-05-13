package com.minangluxe.tender.view;

import javax.swing.*;
import java.awt.*;

public class SellerDashboard extends JFrame {
    private int userId;

    public int getUserId() {
        return userId;
    }
    
    public SellerDashboard(int userId) {
        this.userId = userId;
        setTitle("MinangLuxe — Seller Dashboard (ID: " + userId + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(com.minangluxe.tender.util.StyleUtil.BG_DARK);

        JPanel topBar = createTopBar();

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Inter", Font.BOLD, 13));
        tabs.setBackground(com.minangluxe.tender.util.StyleUtil.BG_CARD);
        tabs.setForeground(com.minangluxe.tender.util.StyleUtil.TEXT_PRIMARY);

        SellerPanel sellerPanel = new SellerPanel();
        JPanel sellerCol = wrapColumn("🏪 Incoming Tenders & Offers", sellerPanel, new Color(100, 180, 255));

        tabs.addTab("📥 Incoming Tenders", sellerCol);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(com.minangluxe.tender.util.StyleUtil.BG_DARK);
        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        add(root);
    }


    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(com.minangluxe.tender.util.StyleUtil.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, com.minangluxe.tender.util.StyleUtil.GOLD_DARK),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);

        JLabel pinkBar = new JLabel("  ");
        pinkBar.setOpaque(true);
        pinkBar.setBackground(com.minangluxe.tender.util.StyleUtil.GOLD_PRIMARY);
        pinkBar.setPreferredSize(new Dimension(5, 40));
        left.add(pinkBar, BorderLayout.WEST);

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);
        JLabel logo = new JLabel("♦ MinangLuxe Seller");
        logo.setFont(new Font("Inter", Font.BOLD, 22));
        logo.setForeground(com.minangluxe.tender.util.StyleUtil.GOLD_PRIMARY);
        JLabel tagline = new JLabel("Premium Tender Platform — From Minang, For the World");
        tagline.setFont(new Font("Inter", Font.ITALIC, 12));
        tagline.setForeground(com.minangluxe.tender.util.StyleUtil.TEXT_MUTED);
        textStack.add(logo);
        textStack.add(tagline);
        left.add(textStack, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        right.setOpaque(false);
        JLabel live = new JLabel("● LIVE");
        live.setFont(new Font("Inter", Font.BOLD, 11));
        live.setForeground(com.minangluxe.tender.util.StyleUtil.SUCCESS);
        live.setOpaque(true);
        live.setBackground(new Color(0, 40, 20));
        live.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(com.minangluxe.tender.util.StyleUtil.SUCCESS, 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        JLabel ver = new JLabel("ID: " + userId);
        ver.setFont(new Font("Inter", Font.PLAIN, 11));
        ver.setForeground(com.minangluxe.tender.util.StyleUtil.TEXT_MUTED);
        right.add(live);
        right.add(ver);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel wrapColumn(String title, JPanel content, Color accentColor) {
        JPanel col = new JPanel(new BorderLayout());
        col.setBackground(com.minangluxe.tender.util.StyleUtil.BG_DARK);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setBackground(com.minangluxe.tender.util.StyleUtil.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, accentColor),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Inter", Font.BOLD, 10));
        dot.setForeground(accentColor);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Inter", Font.BOLD, 14));
        lbl.setForeground(accentColor);
        header.add(dot);
        header.add(lbl);

        col.add(header, BorderLayout.NORTH);
        col.add(content, BorderLayout.CENTER);
        return col;
    }
}