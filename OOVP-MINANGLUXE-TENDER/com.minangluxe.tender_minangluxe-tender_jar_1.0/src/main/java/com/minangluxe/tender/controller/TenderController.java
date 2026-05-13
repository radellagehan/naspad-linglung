package com.minangluxe.tender.controller;

import com.minangluxe.tender.model.*;
import java.util.*;

public class TenderController {
    private static TenderController instance;
    private final List<Seller> sellers = new ArrayList<>();
    private final List<TenderRequest> activeRequests = new ArrayList<>();
    private final Map<String, List<Offer>> offersMap = new HashMap<>();
    private final List<Payment> paymentHistory = new ArrayList<>();
    private final List<TenderListener> listeners = new ArrayList<>();
    private final Buyer currentUser;

    public interface TenderListener {
        void onNewRequest(TenderRequest request);
        void onNewOffer(String requestId, Offer offer);
        default void onPaymentCompleted(Payment payment) {}
    }

    private TenderController() {
        currentUser = new Buyer("B-999", "08111", "Jakarta", 5.0);
        setupDefaultSellers();
    }

    public static TenderController getInstance() {
        if (instance == null) instance = new TenderController();
        return instance;
    }

    private void setupDefaultSellers() {
        String[] contacts = {"0812", "0821", "0857", "0878", "0813", "0819", "0852", "0831", "0845", "0867"};
        String[] addresses = {"Jakarta", "Bandung", "Surabaya", "Medan", "Padang", "Bekasi", "Tangerang", "Bogor", "Depok", "Malang"};
        for (int i = 1; i <= 10; i++) {
            sellers.add(new Seller("Store " + i, contacts[i-1], addresses[i-1], 4.8));
        }
    }

    // --- FUNGSI-FUNGSI YANG DICARI VIEW ---
    public List<Seller> getSellers() { return sellers; }
    public Buyer getCurrentUser() { return currentUser; }
    public List<Payment> getPaymentHistory() { return paymentHistory; }

    public void postRequest(String query, String preferences, String address) {
        TenderRequest req = new TenderRequest(currentUser, query, preferences, address);
        activeRequests.add(req);
        offersMap.put(req.getRequestId(), new ArrayList<>());
        notifyNewRequest(req);
    }

    public void submitOffer(String requestId, Offer offer) {
        if (offersMap.containsKey(requestId)) {
            offersMap.get(requestId).add(offer);
            notifyNewOffer(requestId, offer);
        }
    }

    public List<Offer> getBestOffers(String requestId) {
        return offersMap.getOrDefault(requestId, new ArrayList<>());
    }

    public Payment checkout(List<Offer> selectedOffers, String buyerAddress) {
        Payment payment = new Payment(currentUser, selectedOffers, buyerAddress);
        paymentHistory.add(0, payment);
        notifyPaymentCompleted(payment);
        return payment;
    }

    public Map<String, Integer> parseOrderText(String text) {
        Map<String, Integer> result = new LinkedHashMap<>();
        // Simple Logic
        result.put(text, 1);
        return result;
    }

    public void addListener(TenderListener l) { listeners.add(l); }
    private void notifyNewRequest(TenderRequest r) { listeners.forEach(l -> l.onNewRequest(r)); }
    private void notifyNewOffer(String id, Offer o) { listeners.forEach(l -> l.onNewOffer(id, o)); }
    private void notifyPaymentCompleted(Payment p) { listeners.forEach(l -> l.onPaymentCompleted(p)); }
}