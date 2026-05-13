package com.minangluxe.tender.model;

import java.util.UUID;

public class TenderRequest {
    private final String id;
    private final Buyer buyer;
    private final String query;
    private final String preferences;
    private final String buyerAddress;

    public TenderRequest(Buyer buyer, String query, String preferences) {
        this.id = UUID.randomUUID().toString();
        this.buyer = buyer;
        this.query = query;
        this.preferences = preferences;
        this.buyerAddress = buyer.getAddress();
    }

    public TenderRequest(Buyer buyer, String query, String preferences, String buyerAddress) {
        this.id = UUID.randomUUID().toString();
        this.buyer = buyer;
        this.query = query;
        this.preferences = preferences;
        this.buyerAddress = buyerAddress;
    }

    public String getRequestId()   { return id; }
    public Buyer  getBuyer()       { return buyer; }
    public String getQuery()       { return query; }
    public String getPreferences() { return preferences; }
    public String getBuyerAddress(){ return buyerAddress; }
}
