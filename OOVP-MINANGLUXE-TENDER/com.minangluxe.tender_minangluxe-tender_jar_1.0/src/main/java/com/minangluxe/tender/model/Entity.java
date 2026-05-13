package com.minangluxe.tender.model;

// ABSTRACTION: Class ini 'abstract' karena Entity itu umum, gak bisa dibikin object langsung.
public abstract class Entity {
    // ENCAPSULATION: Pakai 'protected' atau 'private' biar data aman.
    protected String id;
    protected String phoneNumber;
    protected String address;
    protected double rating;

    public Entity(String id, String phoneNumber, String address, double rating) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.rating = rating;
    }

    // Abstract method: Anak-anaknya WAJIB punya cara komunikasi sendiri
    public abstract void communicate(String message);

    // Getter untuk ambil data secara aman
    public String getId() { return id; }
    public double getRating() { return rating; }

    // Tambahin ini di Entity.java lu biar bisa dipanggil dari luar
    public String getPhoneNumber() {
    return phoneNumber; }
    
    public String getAddress() {
    return address; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
}