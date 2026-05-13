package com.minangluxe.tender.util;

import java.sql.*;

public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("Mencoba koneksi ke database...");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3307/minangluxe_db", 
                "root", 
                ""
            );
            
            if (conn != null) {
                System.out.println("✅ SUCCESS! Koneksi database BERHASIL!");
                System.out.println("Database: " + conn.getCatalog());
                conn.close();
            } else {
                System.out.println("❌ GAGAL! Koneksi NULL!");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ ERROR: MySQL Driver tidak ditemukan!");
            System.out.println("Solusi: Tambahkan library mysql-connector-java.jar");
            e.printStackTrace();
            
        } catch (SQLException e) {
            System.out.println("❌ ERROR SQL: " + e.getMessage());
            System.out.println("\n🔧 SOLUSI:");
            System.out.println("1. Pastikan XAMPP MySQL sudah START (tombol Start di XAMPP)");
            System.out.println("2. Pastikan database 'minangluxe' sudah dibuat di phpMyAdmin");
            System.out.println("3. Pastikan username 'root' dan password kosong (default XAMPP)");
            e.printStackTrace();
        }
    }
}