package com.minangluxe.tender.util;

// Import folder model tempat lu nyimpen Entity, Buyer, Seller, Product
import com.minangluxe.tender.model.*;

public class OOPTest {
    public static void main(String[] args) {
        System.out.println("=== MEMULAI SIMULASI AI AGENT & OOP ===");

        // 1. Bikin Objek Buyer dan Seller (Ini ngebuktiin Inheritance)
        Buyer buyer1 = new Buyer("B-001", "08123456", "Jakarta", 4.8);
        Seller seller1 = new Seller("S-001", "08987654", "Padang", 4.9);

        // 2. Pembuktian Polymorphism (Method sama, tapi output beda)
        buyer1.communicate("Gua butuh katering buat acara besok!");
        seller1.communicate("Halo, MinangLuxe siap melayani pesanan Anda.");

        // 3. Pembuktian Object Product
        Product penawaran = new Product("PROD-1", "Paket S2 Premium", 450, 18000);
        seller1.offerProduct(penawaran);

        System.out.println("=======================================");
    }
}