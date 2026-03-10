package com.minangluxe.tender.model;

import java.util.UUID;

public class Seller {
    private final String id;
    private String name;
    private String contactId;
    private String address;

    public Seller(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.contactId = "";
        this.address = "";
    }

    public Seller(String name, String contactId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.contactId = contactId != null ? contactId : "";
        this.address = "";
    }

    public Seller(String name, String contactId, String address) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.contactId = contactId != null ? contactId : "";
        this.address = address != null ? address : "";
    }

    public String getId()        { return id; }
    public String getName()      { return name; }
    public String getContactId() { return contactId; }
    public void   setContactId(String c) { this.contactId = c; }
    public String getAddress()   { return address; }
    public void   setAddress(String a)   { this.address = a != null ? a : ""; }

    @Override public String toString() { return name; }
}
