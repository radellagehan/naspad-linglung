package com.minangluxe.tender.model;

// INHERITANCE: extends Entity
public class Buyer extends Entity {
    
    public Buyer(String id, String phoneNumber, String address, double rating) {
        super(id, phoneNumber, address, rating);
    }

    // POLYMORPHISM (Overriding): Buyer punya cara komunikasi yang beda (meminta/request)
    @Override
    public void communicate(String message) {
        System.out.println("[BUYER " + id + "] Mengirim Request Tender: " + message);
    }

    // Method khusus Buyer
    public void requestItem(String requirements) {
        System.out.println("Mencari item dengan spek: " + requirements);
    }

    public void setAddress(Class<? extends Buyer> buyerAddress) {
        throw new UnsupportedOperationException("Unimplemented method 'setAddress'");
    }

    public String getAddress() {
        throw new UnsupportedOperationException("Unimplemented method 'getAddress'");
    }
}