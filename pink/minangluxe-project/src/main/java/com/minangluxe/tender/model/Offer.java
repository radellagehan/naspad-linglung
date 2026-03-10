package com.minangluxe.tender.model;

public class Offer {
    private final Seller seller;
    private final Product product;
    private final double price;
    private int quantity;
    private final double rating;
    private final String comment;
    private final TenderRequest request;
    private double aiScore;

    public Offer(Seller seller, Product product, double price, int quantity,
                 double rating, String comment, TenderRequest request) {
        this.seller   = seller;
        this.product  = product;
        this.price    = price;
        this.quantity = quantity;
        this.rating   = rating;
        this.comment  = comment;
        this.request  = request;
        calculateScore();
    }

    public Offer(Seller seller, Product product, double price,
                 double rating, String comment, TenderRequest request) {
        this(seller, product, price, 1, rating, comment, request);
    }

    private void calculateScore() {
        this.aiScore = price > 0 ? (rating / price) * 10000 : 0;
    }

    public Seller  getSeller()    { return seller; }
    public Product getProduct()   { return product; }
    public double  getPrice()     { return price; }
    public int     getQuantity()  { return quantity; }
    public void    setQuantity(int q) { this.quantity = Math.max(1, q); calculateScore(); }
    public double  getTotalPrice(){ return price * quantity; }
    public double  getRating()    { return rating; }
    public String  getComment()   { return comment; }
    public double  getAiScore()   { return aiScore; }

    @Override public String toString() {
        return String.format("%s — %s x%d @ Rp%.0f (%.1f★)",
            seller.getName(), product.getName(), quantity, price, rating);
    }
}
