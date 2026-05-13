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
    private String imagePath;

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

    private void calculateScore() {
        // Rumus AI Score untuk ranking
        this.aiScore = price > 0 ? (rating / price) * 10000 : 0;
    }

    // GETTER-GETTER PENTING AGAR FILE LAIN TIDAK MERAH
    public Seller getSeller()      { return seller; }
    public Product getProduct()    { return product; }
    public double getPrice()       { return price; }
    public int getQuantity()       { return quantity; }
    public double getRating()      { return rating; }
    public String getComment()     { return comment; }
    public double getAiScore()     { return aiScore; }
    public TenderRequest getRequest() { return request; }
    public String getImagePath()   { return imagePath; }
    
    // Method ini yang dicari oleh Payment.java
    public double getTotalPrice()  { return price * quantity; }

    public void setQuantity(int q) { 
        this.quantity = Math.max(1, q); 
        calculateScore(); 
    }

    @Override public String toString() {
        return String.format("%s — %s x%d @ Rp%.0f (%.1f★)",
            seller.getName(), product.getName(), quantity, price, rating);
    }
}