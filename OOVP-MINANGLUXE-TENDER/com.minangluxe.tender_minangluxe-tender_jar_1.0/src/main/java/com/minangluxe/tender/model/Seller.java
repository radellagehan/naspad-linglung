package com.minangluxe.tender.model;

public class Seller extends Entity {

    public Seller(String id, String phoneNumber, String address, double rating) {
        super(id, phoneNumber, address, rating);
    }

    // POLYMORPHISM (Overriding): Seller ngomongnya beda (menawarkan)
    @Override
    public void communicate(String message) {
        System.out.println("[SELLER " + id + "] Menawarkan Produk: " + message);
    }

    // Method khusus Seller
    public void offerProduct(Product product) {
        System.out.println("Menawarkan: " + product.getName() + " seharga Rp" + product.getPrice());
    }

    public String getName() {
        return id;
    }

    public String getContactId() {
        return phoneNumber;
    }
}