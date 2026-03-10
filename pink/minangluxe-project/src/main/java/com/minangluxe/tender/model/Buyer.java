package com.minangluxe.tender.model;

import java.util.UUID;

public class Buyer {
    private final String id;
    private final String name;
    private String address;

    public Buyer(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.address = "";
    }

    public String getId()      { return id; }
    public String getName()    { return name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
