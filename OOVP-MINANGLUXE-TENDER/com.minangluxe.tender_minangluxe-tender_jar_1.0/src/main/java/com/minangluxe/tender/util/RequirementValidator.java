package com.minangluxe.tender.util;

import com.minangluxe.tender.model.Offer;
import com.minangluxe.tender.model.TenderRequirement;

public class RequirementValidator {

    public static class ValidationResult {
        public final boolean accepted;
        public final String reason;

        public ValidationResult(boolean accepted, String reason) {
            this.accepted = accepted;
            this.reason = reason;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, "OK");
        }

        public static ValidationResult fail(String reason) {
            return new ValidationResult(false, reason);
        }
    }

    /**
     * Validasi sederhana berbasis data yang tersedia di Offer (price & quantity).
     * Paket/berat belum bisa divalidasi ketat karena Offer saat ini tidak menyimpan paket/berat.
     */
    public static ValidationResult validate(TenderRequirement req, Offer offer) {
        if (req == null || offer == null) return ValidationResult.fail("Missing requirement");

        long totalPrice = Math.round(offer.getPrice() * offer.getQuantity());

        // Budget total
        if (req.getBudgetTotalRp() != null) {
            if (totalPrice > req.getBudgetTotalRp()) {
                return ValidationResult.fail("Total price exceeds budget total");
            }
        }

        // Budget per item: offer.price must be <= budgetPerItem
        if (req.getBudgetPerItemRp() != null) {
            if (offer.getPrice() > req.getBudgetPerItemRp()) {
                return ValidationResult.fail("Unit price exceeds budget per item");
            }
        }

        // Quantity total match (strict)
        if (req.getQtyTotal() != null) {
            if (offer.getQuantity() != req.getQtyTotal()) {
                return ValidationResult.fail("Quantity does not match required qty");
            }
        }

        return ValidationResult.ok();
    }
}

