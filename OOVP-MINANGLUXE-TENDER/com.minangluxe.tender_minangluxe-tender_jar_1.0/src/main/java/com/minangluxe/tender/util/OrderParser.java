package com.minangluxe.tender.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderParser {

    public static Map<String, String> parseToMap(String input) {
        Map<String, String> results = new HashMap<>();
        if (input == null || input.trim().isEmpty()) {
            return results;
        }

        String text = input.toLowerCase();

        // === Paket Detection ===
        if (text.contains("s1") || text.contains("paket 1")) {
            results.put("paket", "S1");
        } else if (text.contains("s2") || text.contains("paket 2")) {
            results.put("paket", "S2");
        } else if (text.contains("s3") || text.contains("paket 3")) {
            results.put("paket", "S3");
        } else if (text.contains("basic") || text.contains("b ")) {
            results.put("paket", "B");
        }

        // === Berat / Weight (gram) ===
        Matcher beratMatcher = Pattern.compile("(\\d+)\\s*(?:gr|gram|grams|g\\b)").matcher(text);
        if (beratMatcher.find()) {
            results.put("berat", beratMatcher.group(1));
        }

        // === Harga (support "35k", "35000", "35.000", etc.) ===
        Matcher hargaMatcher = Pattern.compile("(\\d+[.,]?\\d*)\\s*[kK]").matcher(text);
        if (hargaMatcher.find()) {
            String num = hargaMatcher.group(1).replace(",", "").replace(".", "");
            int harga = Integer.parseInt(num) * 1000;
            results.put("harga", String.valueOf(harga));
        } else {
            // Direct full number
            Matcher fullPrice = Pattern.compile("(\\d{4,})").matcher(text);
            if (fullPrice.find()) {
                results.put("harga", fullPrice.group(1));
            }
        }

        return results;
    }
}