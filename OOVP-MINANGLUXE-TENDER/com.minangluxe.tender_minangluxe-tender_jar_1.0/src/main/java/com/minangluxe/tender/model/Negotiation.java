package com.minangluxe.tender.model;

import java.time.LocalDateTime;

public class Negotiation {
    private final int id;
    private final int requestId;
    private final int offerId;
    private final String sellerName;
    private final Integer buyerId;

    private final double buyerPrice;
    private final Double sellerPrice;
    private final NegotiationStatus status;

    // optional
    private final LocalDateTime createdAt;

    public Negotiation(int id,
                        int requestId,
                        int offerId,
                        String sellerName,
                        Integer buyerId,
                        double buyerPrice,
                        Double sellerPrice,
                        NegotiationStatus status,
                        LocalDateTime createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.offerId = offerId;
        this.sellerName = sellerName;
        this.buyerId = buyerId;
        this.buyerPrice = buyerPrice;
        this.sellerPrice = sellerPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getRequestId() { return requestId; }
    public int getOfferId() { return offerId; }
    public String getSellerName() { return sellerName; }
    public Integer getBuyerId() { return buyerId; }

    public double getBuyerPrice() { return buyerPrice; }
    public Double getSellerPrice() { return sellerPrice; }
    public NegotiationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

