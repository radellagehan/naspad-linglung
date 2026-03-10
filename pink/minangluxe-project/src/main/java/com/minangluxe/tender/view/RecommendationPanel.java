package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RecommendationPanel extends JPanel implements TenderController.TenderListener {

    private final BuyerPanel buyerPanel;
    private JPanel contentPanel;
    private final Map<String, JPanel> requestCards = new LinkedHashMap<>();

    public RecommendationPanel(BuyerPanel buyerPanel) {
        this.buyerPanel = buyerPanel;
        setLayout(new BorderLayout(0, 0));
        setBackground(StyleUtil.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        TenderController.getInstance().addListener(this);

        // Top bar with Reset button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel countLabel = new JLabel("0 requests");
        countLabel.setFont(StyleUtil.FONT_SMALL);
        countLabel.setForeground(StyleUtil.TEXT_MUTED);

        JButton resetBtn = new JButton("↺  Reset All");
        StyleUtil.styleActionButton(resetBtn, StyleUtil.DANGER);
        resetBtn.setFont(StyleUtil.FONT_SMALL);
        resetBtn.addActionListener(e -> {
            contentPanel.removeAll();
            requestCards.clear();
            contentPanel.revalidate();
            contentPanel.repaint();
            countLabel.setText("0 requests");
        });

        topBar.add(countLabel, BorderLayout.WEST);
        topBar.add(resetBtn,   BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        contentPanel.setBackground(StyleUtil.BG_DARK);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setBackground(StyleUtil.BG_DARK);
        scroll.getViewport().setBackground(StyleUtil.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // update count helper — store ref
        contentPanel.putClientProperty("countLabel", countLabel);
    }

    @Override
    public void onNewRequest(TenderRequest request) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(StyleUtil.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setPreferredSize(new Dimension(280, 160));

        JLabel lbl = new JLabel("<html><div style='width:230px;color:#ff69b4;font-weight:bold;'>"
            + "✦ " + request.getQuery() + "</div></html>");
        lbl.setFont(StyleUtil.FONT_BOLD);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(6));

        JLabel wait = new JLabel("<html><span style='color:#8090c0;font-size:11px;'>Waiting for offers...</span></html>");
        wait.setFont(StyleUtil.FONT_SMALL);
        wait.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(wait);

        requestCards.put(request.getRequestId(), card);
        contentPanel.add(card);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateCount();
    }

    @Override
    public void onNewOffer(String requestId, Offer offer) {
        JPanel card = requestCards.get(requestId);
        if (card != null) renderOffers(card, requestId);
    }

    private void renderOffers(JPanel card, String requestId) {
        card.removeAll();
        card.setPreferredSize(null);

        List<Offer> offers = TenderController.getInstance().getBestOffers(requestId);
        if (offers.isEmpty()) {
            JLabel e = new JLabel("No offers yet.");
            e.setFont(StyleUtil.FONT_SMALL);
            e.setForeground(StyleUtil.TEXT_MUTED);
            card.add(e);
            card.revalidate(); card.repaint();
            return;
        }

        JLabel hdr = new JLabel("<html><div style='width:240px;color:#ff69b4;'>"
            + "<b>Best Offers</b><br>"
            + "<span style='font-size:11px;color:#8090c0;'>Select a package or items to add to cart</span></div></html>");
        hdr.setFont(StyleUtil.FONT_BOLD);
        hdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hdr);
        card.add(Box.createVerticalStrut(8));

        Map<Seller, List<Offer>> bySeller = new LinkedHashMap<>();
        for (Offer o : offers) bySeller.computeIfAbsent(o.getSeller(), k -> new ArrayList<>()).add(o);

        for (Map.Entry<Seller, List<Offer>> entry : bySeller.entrySet()) {
            Seller seller = entry.getKey();
            List<Offer> sellerOffers = entry.getValue();
            double pkg = sellerOffers.stream().mapToDouble(Offer::getTotalPrice).sum();
            double avgScore = sellerOffers.stream().mapToDouble(Offer::getAiScore).average().orElse(0);

            JLabel sellerLbl = new JLabel(
                "<html><div style='width:240px;'>"
                + "<b style='color:#ff9ec8;'>" + seller.getName() + "</b>"
                + "<span style='font-size:11px;color:#8090c0;'> — Package: Rp "
                + StyleUtil.formatRupiah(pkg) + " | AI Score: " + String.format("%.1f", avgScore) + "</span>"
                + "</div></html>");
            sellerLbl.setFont(StyleUtil.FONT_BODY);
            sellerLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(sellerLbl);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            btnRow.setOpaque(false);
            JButton addPkg = new JButton("✦ Add Package");
            StyleUtil.styleActionButton(addPkg, StyleUtil.GOLD_PRIMARY);
            addPkg.setFont(StyleUtil.FONT_SMALL);
            addPkg.addActionListener(e -> sellerOffers.forEach(buyerPanel::addOfferToCart));
            btnRow.add(addPkg);
            btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(btnRow);

            JPanel items = new JPanel();
            items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
            items.setOpaque(false);

            List<JCheckBox> checks = new ArrayList<>();
            for (Offer o : sellerOffers) {
                String stars = StyleUtil.ICON_STAR.repeat((int) Math.round(o.getRating()));
                JCheckBox cb = new JCheckBox(
                    "<html><div style='width:230px;color:#ccc;font-size:12px;'>"
                    + "◆ " + o.getProduct().getName() + " x" + o.getQuantity()
                    + " — Rp " + StyleUtil.formatRupiah(o.getTotalPrice())
                    + " <span style='color:#ff9ec8;'>" + stars + "</span>"
                    + "</div></html>");
                cb.setOpaque(false);
                cb.setFont(StyleUtil.FONT_SMALL);
                cb.setForeground(StyleUtil.TEXT_PRIMARY);
                cb.putClientProperty("offer", o);
                items.add(cb);
                checks.add(cb);
            }
            items.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(items);

            JButton addSel = new JButton("Add Selected");
            StyleUtil.styleActionButton(addSel, StyleUtil.BG_ELEVATED);
            addSel.setForeground(StyleUtil.GOLD_ACCENT);
            addSel.setFont(StyleUtil.FONT_SMALL);
            addSel.addActionListener(e -> checks.stream()
                .filter(JCheckBox::isSelected)
                .forEach(cb -> buyerPanel.addOfferToCart((Offer) cb.getClientProperty("offer"))));

            JPanel selRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            selRow.setOpaque(false);
            selRow.add(addSel);
            selRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(selRow);
            card.add(Box.createVerticalStrut(8));
            card.add(StyleUtil.goldSeparator());
            card.add(Box.createVerticalStrut(8));
        }

        card.revalidate();
        card.repaint();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void updateCount() {
        Object lbl = contentPanel.getClientProperty("countLabel");
        if (lbl instanceof JLabel) {
            ((JLabel) lbl).setText(requestCards.size() + " request" + (requestCards.size() != 1 ? "s" : ""));
        }
    }

    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension d = layoutSize(target, false); d.width -= (getHgap() + 1); return d;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                Container c = target;
                while (c.getSize().width == 0 && c.getParent() != null) c = c.getParent();
                targetWidth = c.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets ins = target.getInsets();
                int maxW = targetWidth - (ins.left + ins.right + hgap * 2);
                Dimension dim = new Dimension(0, 0);
                int rowW = 0, rowH = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowW + d.width > maxW) {
                            dim.width = Math.max(dim.width, rowW);
                            dim.height += rowH + vgap;
                            rowW = 0; rowH = 0;
                        }
                        rowW += d.width + hgap;
                        rowH = Math.max(rowH, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowW);
                dim.height += rowH + vgap;
                dim.width += ins.left + ins.right + hgap * 2;
                dim.height += ins.top + ins.bottom + vgap * 2;
                return dim;
            }
        }
    }
}
