package com.minangluxe.tender.view;

import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("MinangLuxe — Premium Tender Marketplace");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleUtil.BG_DARK);

        JPanel topBar = createTopBar();

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(StyleUtil.BG_CARD);
        tabs.setForeground(StyleUtil.TEXT_PRIMARY);

        // Layout: Seller (kiri) | Buyer (tengah) | Rekomendasi (kanan)
        BuyerPanel buyerPanel        = new BuyerPanel();
        RecommendationPanel recPanel = new RecommendationPanel(buyerPanel);
        SellerPanel sellerPanel      = new SellerPanel();

        JPanel sellerCol = wrapColumn(" Kitchen Seller",      sellerPanel, new Color(100, 180, 255));
        JPanel buyerCol  = wrapColumn(" Buyer",     buyerPanel,  new Color(255, 105, 180));
        JPanel recCol    = wrapColumn(" Best Offer",  recPanel,    new Color(200, 140, 255));

        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sellerCol, buyerCol);
        leftSplit.setResizeWeight(0.5);
        leftSplit.setDividerSize(4);
        leftSplit.setBackground(StyleUtil.BG_DARK);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, recCol);
        mainSplit.setResizeWeight(0.67);
        mainSplit.setDividerSize(4);
        mainSplit.setBackground(StyleUtil.BG_DARK);

        tabs.addTab("  Tender Market",    mainSplit);
        tabs.addTab("  Order History", new OrderHistoryPanel());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(StyleUtil.BG_DARK);
        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs,   BorderLayout.CENTER);
        add(root);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(StyleUtil.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, StyleUtil.GOLD_DARK),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Pink accent bar on left
        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);

        JLabel pinkBar = new JLabel("  ");
        pinkBar.setOpaque(true);
        pinkBar.setBackground(StyleUtil.GOLD_PRIMARY);
        pinkBar.setPreferredSize(new Dimension(5, 40));
        left.add(pinkBar, BorderLayout.WEST);

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);
        JLabel logo = new JLabel("♦ MinangLuxe");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(StyleUtil.GOLD_PRIMARY);
        JLabel tagline = new JLabel("Original MinangWak — Local Grit, Global Hit");
        tagline.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tagline.setForeground(StyleUtil.TEXT_MUTED);
        textStack.add(logo);
        textStack.add(tagline);
        left.add(textStack, BorderLayout.CENTER);

        // Right: live pill + version
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        right.setOpaque(false);
        JLabel live = new JLabel("● LIVE");
        live.setFont(new Font("SansSerif", Font.BOLD, 11));
        live.setForeground(StyleUtil.SUCCESS);
        live.setOpaque(true);
        live.setBackground(new Color(0, 40, 20));
        live.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.SUCCESS, 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        JLabel ver = new JLabel("v1.0");
        ver.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ver.setForeground(StyleUtil.TEXT_MUTED);
        right.add(live);
        right.add(ver);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel wrapColumn(String title, JPanel content, Color accentColor) {
        JPanel col = new JPanel(new BorderLayout());
        col.setBackground(StyleUtil.BG_DARK);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setBackground(StyleUtil.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, accentColor),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.BOLD, 10));
        dot.setForeground(accentColor);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(accentColor);
        header.add(dot);
        header.add(lbl);

        col.add(header,  BorderLayout.NORTH);
        col.add(content, BorderLayout.CENTER);
        return col;
    }
}
