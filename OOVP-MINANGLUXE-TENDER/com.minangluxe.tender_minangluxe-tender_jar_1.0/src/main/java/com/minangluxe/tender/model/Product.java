package com.minangluxe.tender.model;

public class Product {
    private String id;
    private String name;
    private int weightGram;
    private int price;

    public Product(String id, String name, int weightGram, int price) {
        this.id = id;
        this.name = name;
        this.weightGram = weightGram;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getWeight() { return weightGram; }
}