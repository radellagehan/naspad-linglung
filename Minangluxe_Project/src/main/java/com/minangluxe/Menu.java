package com.minangluxe;

public class Menu {
    private final int id;
    private final String namaMenu;
    private final int harga;

    public Menu(int id, String namaMenu, int harga) {
        this.id = id;
        this.namaMenu = namaMenu;
        this.harga = harga;
    }
    public int getId() { return id; }
    public String getNamaMenu() { return namaMenu; }
    public int getHarga() { return harga; }
}