package com.minangluxe.tender.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class Payment {
    private final String id;
    private final Buyer buyer;
    private final List<Offer> items;
    private final double totalAmount;
    private final String buyerAddress;
    private final LocalDateTime timestamp;

    public Payment(Buyer buyer, List<Offer> items, String buyerAddress) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.buyer = buyer;
        this.items = items;
        this.buyerAddress = buyerAddress;
        this.timestamp = LocalDateTime.now();
        this.totalAmount = items.stream().mapToDouble(Offer::getTotalPrice).sum();
    }

    public String        getId()              { return id; }
    public Buyer         getBuyer()           { return buyer; }
    public List<Offer>   getItems()           { return items; }
    public double        getTotalAmount()     { return totalAmount; }
    public String        getBuyerAddress()    { return buyerAddress; }
    public LocalDateTime getTimestamp()       { return timestamp; }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    @Override public String toString() {
        return String.format("Order #%s — Rp %,.0f (%s)", id, totalAmount, getFormattedTimestamp());
    }
}
