package com.minangluxe.tender.view;

import com.minangluxe.tender.controller.TenderController;
import com.minangluxe.tender.model.*;
import com.minangluxe.tender.util.DatabaseConnection;
import com.minangluxe.tender.util.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SellerPanel extends JPanel implements TenderController.TenderListener {

    private DefaultListModel<String> requestModel;
    private JList<String> requestList;
    private JPanel cardsContainer;
    private final List<SellerInputCard> sellerCards = new ArrayList<>();

    /** Parallel list — index i maps to requestModel item i's DB primary key */
    private final List<Integer> dbRequestIds = new ArrayList<>();
    /** DB IDs already loaded to avoid duplicates */
    private final Set<Integer> loadedIds = new HashSet<>();

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
        requestList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) syncCards();
        });

        JScrollPane listScroll = new JScrollPane(requestList);
        listScroll.setBackground(StyleUtil.BG_CARD);
        listScroll.getViewport().setBackground(StyleUtil.BG_CARD);
        listScroll.setBorder(BorderFactory.createLineBorder(StyleUtil.GOLD_DARK, 1, true));
        listScroll.setPreferredSize(new Dimension(240, 0));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(StyleUtil.BG_CARD);

        JPanel listHeaderPanel = new JPanel(new BorderLayout(8, 0));
        listHeaderPanel.setBackground(StyleUtil.BG_CARD);
        listHeaderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.GOLD_DARK),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JLabel listHdr = new JLabel("  Incoming Requests");
        listHdr.setFont(StyleUtil.FONT_BOLD);
        listHdr.setForeground(new Color(100, 180, 255));
        JLabel liveLabel = new JLabel("● LIVE");
        liveLabel.setFont(StyleUtil.FONT_SMALL);
        liveLabel.setForeground(StyleUtil.SUCCESS);
        listHeaderPanel.add(listHdr,   BorderLayout.WEST);
        
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        
        JButton resetBtn = new JButton("↺ Reset");
        StyleUtil.styleLinkButton(resetBtn);
        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to PERMANENTLY clear all incoming tenders?\nThis will delete data for all users.", 
                "Permanent Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                com.minangluxe.tender.util.DatabaseConnection.executeUpdate("DELETE FROM tender_offers");
                com.minangluxe.tender.util.DatabaseConnection.executeUpdate("DELETE FROM tender_requests");
                requestModel.clear();
                dbRequestIds.clear();
                loadedIds.clear();
                loadRequestsFromDB();
                JOptionPane.showMessageDialog(this, "All records have been cleared.");
            }
        });
        
        headerActions.add(resetBtn);
        headerActions.add(liveLabel);
        listHeaderPanel.add(headerActions, BorderLayout.EAST);

        listPanel.add(listHeaderPanel, BorderLayout.NORTH);
        listPanel.add(listScroll,      BorderLayout.CENTER);
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

        // Load existing requests immediately on open
        loadRequestsFromDB();

        // Poll DB every 3 seconds for new buyer requests
        javax.swing.Timer pollTimer = new javax.swing.Timer(3000, e -> loadRequestsFromDB());
        pollTimer.setInitialDelay(3000);
        pollTimer.start();
    }

    /** Reads new rows from tender_requests and appends them to the list. */
    private void loadRequestsFromDB() {
        new Thread(() -> {
            List<String[]> newRows = new ArrayList<>();
            List<Integer>  newIds  = new ArrayList<>();

            String sql = "SELECT id, buyer_id, query, address, created_at " +
                         "FROM tender_requests ORDER BY created_at ASC";
            java.sql.Connection conn = DatabaseConnection.getConnection();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int    dbId    = rs.getInt("id");
                    int    buyerId = rs.getInt("buyer_id");
                    String query   = rs.getString("query");
                    String address = rs.getString("address");

                    if (!loadedIds.contains(dbId)) {
                        newIds.add(dbId);
                        newRows.add(new String[]{
                            String.valueOf(buyerId),
                            query,
                            address == null ? "" : address
                        });
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            if (!newRows.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < newRows.size(); i++) {
                        String[] row = newRows.get(i);
                        int dbId = newIds.get(i);
                        loadedIds.add(dbId);
                        dbRequestIds.add(dbId);      // keep in sync with requestModel
                        String entry = "<html><b style='color:#ff69b4;'>" + row[1] + "</b>"
                            + "<br><span style='font-size:10px;color:#8090c0;'>📍 " + row[2]
                            + " &nbsp;|&nbsp; Buyer #" + row[0] + "</span></html>";
                        requestModel.addElement(entry);
                    }
                    if (requestList.getSelectedIndex() < 0 && requestModel.size() > 0) {
                        requestList.setSelectedIndex(0);
                    }
                });
            }
        }, "db-poll-thread").start();
    }

    /** Tells every SellerInputCard which request is selected (both in-memory and DB ID). */
    private void syncCards() {
        int idx = requestList.getSelectedIndex();
        if (idx < 0 || idx >= dbRequestIds.size()) {
            sellerCards.forEach(c -> { c.setRequest(null); c.setDbRequestId(-1); });
            return;
        }
        int dbId = dbRequestIds.get(idx);
        // Create a lightweight placeholder TenderRequest so cards can be enabled
        TenderRequest placeholder = new TenderRequest(
            TenderController.getInstance().getCurrentUser(),
            requestList.getSelectedValue() == null ? "" : requestList.getSelectedValue(),
            "", ""
        );
        sellerCards.forEach(c -> {
            c.setRequest(placeholder);
            c.setDbRequestId(dbId);   // ← this links the offer to the correct DB row
        });
    }

    @Override public void onNewRequest(TenderRequest request) {
        // Same-JVM path (buyer and seller in one process)
        String entry = "<html><b style='color:#ff69b4;'>" + request.getQuery() + "</b>"
            + "<br><span style='font-size:10px;color:#8090c0;'>📍 "
            + request.getBuyerAddress() + "</span></html>";
        SwingUtilities.invokeLater(() -> {
            requestModel.addElement(entry);
            dbRequestIds.add(-1); // no DB row for in-process requests
            if (requestModel.size() == 1) requestList.setSelectedIndex(0);
        });
    }
    @Override public void onNewOffer(String requestId, Offer offer) {}

    private static class RequestCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean selected, boolean focused) {
            super.getListCellRendererComponent(list, val, idx, selected, focused);
            if (val instanceof String text) {
                setText(text);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BG_ELEVATED));
                setBackground(selected ? new Color(40, 20, 60) : StyleUtil.BG_CARD);
                setForeground(StyleUtil.TEXT_PRIMARY);
            }
            return this;
        }
    }
}
