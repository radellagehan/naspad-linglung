package com.minangluxe.tender.util;

import javax.swing.*;

public class ValidationUtil {
    
    // Validasi harga tidak negatif
    public static double validatePrice(String priceText, JComponent parent) {
        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                throw new IllegalArgumentException("Harga harus lebih dari 0!");
            }
            return price;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("Invalid number format");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    // Validasi rating (1.0 - 5.0)
    public static double validateRating(String ratingText, JComponent parent) {
        try {
            double rating = Double.parseDouble(ratingText);
            if (rating < 1.0 || rating > 5.0) {
                throw new IllegalArgumentException("Rating harus antara 1.0 - 5.0!");
            }
            return rating;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Rating harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("Invalid number format");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    // Validasi nama produk tidak boleh kosong
    public static String validateProductName(String name, JComponent parent) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Nama produk tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("Product name is empty");
        }
        return name.trim();
    }
}