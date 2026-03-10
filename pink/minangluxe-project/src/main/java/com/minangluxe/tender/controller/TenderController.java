package com.minangluxe.tender.controller;

import com.minangluxe.tender.model.*;
import java.util.*;

/**
 * Singleton Controller — Mediator between Buyer and Sellers.
 */
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
        currentUser = new Buyer("Sultan Buyer " + StyleUtil.ICON_BUYER);
        setupDefaultSellers();
    }

    // small helper ref
    private static final class StyleUtil {
        static final String ICON_BUYER = "👑";
    }

    public static TenderController getInstance() {
        if (instance == null) instance = new TenderController();
        return instance;
    }

    private void setupDefaultSellers() {
        String[] contacts = {
            "081234567890","082198765432","085711112222",
            "087833334444","081355556666","081977778888",
            "085299990000","083122223333","084544445555","086766667777"
        };
        String[] addresses = {
            "Jl. Sudirman No. 12, Jakarta Pusat",
            "Jl. Thamrin 45, Jakarta Selatan",
            "Jl. Gatot Subroto 88, Jakarta",
            "Jl. Rasuna Said Kav 1, Kuningan",
            "Jl. MH Thamrin 28, Jakarta",
            "Jl. HR Rasuna Said, Setiabudi",
            "Jl. Jenderal Sudirman Kav 52, Jakarta",
            "Jl. Prof. Dr. Satrio Kav 18, Jakarta",
            "Jl. Kuningan Barat 1, Jakarta",
            "Jl. Senopati 23, Kebayoran Baru"
        };
        for (int i = 1; i <= 10; i++) {
            sellers.add(new Seller("Seller #" + i, contacts[i - 1], addresses[i - 1]));
        }
    }

    public List<Seller>  getSellers()       { return sellers; }
    public Buyer         getCurrentUser()   { return currentUser; }
    public List<Payment> getPaymentHistory(){ return Collections.unmodifiableList(paymentHistory); }

    public void addListener(TenderListener l) { listeners.add(l); }

    public void postRequest(String query, String preferences, String buyerAddress) {
        currentUser.setAddress(buyerAddress);
        TenderRequest req = new TenderRequest(currentUser, query, preferences, buyerAddress);
        activeRequests.add(req);
        offersMap.put(req.getRequestId(), new ArrayList<>());
        notifyNewRequest(req);
    }

    public void postRequest(String query, String preferences) {
        postRequest(query, preferences, currentUser.getAddress());
    }

    public void submitOffer(String requestId, Offer offer) {
        List<Offer> offers = offersMap.get(requestId);
        if (offers != null) {
            offers.add(offer);
            notifyNewOffer(requestId, offer);
        }
    }

    public List<Offer> getBestOffers(String requestId) {
        List<Offer> offers = offersMap.getOrDefault(requestId, new ArrayList<>());
        List<Offer> sorted = new ArrayList<>(offers);
        sorted.sort(Comparator.comparingDouble(Offer::getAiScore).reversed());
        return sorted;
    }

    public Payment checkout(List<Offer> selectedOffers, String buyerAddress) {
        if (selectedOffers == null || selectedOffers.isEmpty()) return null;
        currentUser.setAddress(buyerAddress);
        Payment payment = new Payment(currentUser, new ArrayList<>(selectedOffers), buyerAddress);
        paymentHistory.add(0, payment);
        notifyPaymentCompleted(payment);
        return payment;
    }

    /**
     * Smart order parser: "rendang 2 nasi 3" → {rendang:2, nasi:3}
     */
    public Map<String, Integer> parseOrderText(String text) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (text == null || text.trim().isEmpty()) return result;
        String[] tokens = text.trim().split("\\s+");
        List<String> nameTokens = new ArrayList<>();
        for (String token : tokens) {
            try {
                int qty = Integer.parseInt(token);
                if (!nameTokens.isEmpty()) {
                    result.put(String.join(" ", nameTokens).trim(), qty);
                    nameTokens.clear();
                }
            } catch (NumberFormatException e) {
                nameTokens.add(token);
            }
        }
        if (!nameTokens.isEmpty()) result.put(String.join(" ", nameTokens).trim(), 1);
        return result;
    }

    private void notifyNewRequest(TenderRequest r)         { listeners.forEach(l -> l.onNewRequest(r)); }
    private void notifyNewOffer(String id, Offer o)        { listeners.forEach(l -> l.onNewOffer(id, o)); }
    private void notifyPaymentCompleted(Payment p)         { listeners.forEach(l -> l.onPaymentCompleted(p)); }
}
