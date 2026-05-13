package com.minangluxe.tender.model;

public class CartItem {
    private final int offerId;
    private final int requestId;
    private final String sellerName;

    private final Seller seller;
    private final Product product;
    private final double price;
    private final int quantity;
    private final double rating;

    public CartItem(int offerId, int requestId, String sellerName,
                     Seller seller, Product product, double price, int quantity, double rating) {
        this.offerId = offerId;
        this.requestId = requestId;
        this.sellerName = sellerName;
        this.seller = seller;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.rating = rating;
    }

    public int getOfferId() { return offerId; }
    public int getRequestId() { return requestId; }
    public String getSellerName() { return sellerName; }

    public Seller getSeller() { return seller; }
    public Product getProduct() { return product; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getRating() { return rating; }

    public double getTotalPrice() { return price * quantity; }

    /** Convert to existing Offer model for checkout flow. */
    public Offer toOffer(TenderRequest requestRef) {
        return new Offer(seller, product, price, quantity, rating, "", requestRef);
    }
}

