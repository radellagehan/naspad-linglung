package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SellerPanel extends JPanel implements TenderController.TenderListener {

    private DefaultListModel<TenderRequest> requestModel;
    private JList<TenderRequest> requestList;
    private JPanel cardsContainer;
    private final List<SellerInputCard> sellerCards = new ArrayList<>();

    public SellerPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(StyleUtil.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        TenderController.getInstance().addListener(this);

        requestModel = new DefaultListModel<>();
        requestList  = new JList<>(requestModel);
        requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestList.setFont(StyleUtil.FONT_BODY);
        requestList.setBackground(StyleUtil.BG_CARD);
        requestList.setForeground(StyleUtil.TEXT_PRIMARY);
        requestList.setFixedCellHeight(60);
        requestList.setCellRenderer(new RequestCellRenderer());
        requestList.addListSelectionListener(e -> syncCards());

        JScrollPane listScroll = new JScrollPane(requestList);
        listScroll.setBackground(StyleUtil.BG_CARD);
        listScroll.getViewport().setBackground(StyleUtil.BG_CARD);
        listScroll.setBorder(BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true));
        listScroll.setPreferredSize(new Dimension(220, 0));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(StyleUtil.BG_CARD);
        JLabel listHdr = new JLabel("  Incoming Requests");
        listHdr.setFont(StyleUtil.FONT_BOLD);
        listHdr.setForeground(new Color(100, 180, 255));
        listHdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.GOLD_DARK),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        listPanel.add(listHdr,    BorderLayout.NORTH);
        listPanel.add(listScroll, BorderLayout.CENTER);
        add(listPanel, BorderLayout.WEST);

        cardsContainer = new JPanel(new GridLayout(0, 1, 10, 10));
        cardsContainer.setBackground(StyleUtil.BG_DARK);

        for (Seller seller : TenderController.getInstance().getSellers()) {
            SellerInputCard card = new SellerInputCard(seller);
            sellerCards.add(card);
            cardsContainer.add(card);
        }

        JScrollPane cardsScroll = new JScrollPane(cardsContainer);
        cardsScroll.setBorder(null);
        cardsScroll.setBackground(StyleUtil.BG_DARK);
        cardsScroll.getViewport().setBackground(StyleUtil.BG_DARK);
        cardsScroll.getVerticalScrollBar().setUnitIncrement(20);
        add(cardsScroll, BorderLayout.CENTER);
    }

    private void syncCards() {
        TenderRequest sel = requestList.getSelectedValue();
        sellerCards.forEach(c -> c.setRequest(sel));
    }

    @Override public void onNewRequest(TenderRequest request) {
        requestModel.addElement(request);
        if (requestModel.size() == 1) requestList.setSelectedIndex(0);
    }
    @Override public void onNewOffer(String requestId, Offer offer) {}

    private static class RequestCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean selected, boolean focused) {
            super.getListCellRendererComponent(list, val, idx, selected, focused);
            if (val instanceof TenderRequest req) {
                setText("<html><b style='color:#ff69b4;'>" + req.getQuery() + "</b>"
                    + "<br><span style='font-size:10px;color:#8090c0;'>" + req.getBuyerAddress() + "</span></html>");
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BG_ELEVATED));
                setBackground(selected ? new Color(40, 20, 60) : StyleUtil.BG_CARD);
                setForeground(StyleUtil.TEXT_PRIMARY);
            }
            return this;
        }
    }
}
