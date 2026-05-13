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

        // === Quantity total (support patterns)
        // Interpretasi kebutuhan (sesuai konfirmasi Anda):
        // - "2 hot 3" => qtyTotal=2, qtyHot=3
        // - "qty 5" / "quantity 5" => qtyTotal=5
        // - "3x21k" => qtyTotal=3 (diambil dari angka sebelum 'x')
        Integer qtyTotal = null;
        Integer qtyHot = null;

        // Prefer explicit "<a> hot <b>"
        Matcher explicitHot = Pattern.compile("(\\d+)\\s*hot\\s*(\\d+)").matcher(text);
        if (explicitHot.find()) {
            qtyTotal = Integer.parseInt(explicitHot.group(1));
            qtyHot = Integer.parseInt(explicitHot.group(2));
        } else {
            Matcher qtyMatcher = Pattern.compile("(?:qty|quantity)\\s*(\\d+)").matcher(text);
            if (qtyMatcher.find()) {
                qtyTotal = Integer.parseInt(qtyMatcher.group(1));
            } else {
                // fallback: capture leading number in patterns like "3x21k"
                Matcher xLeading = Pattern.compile("\\b(\\d+)\\s*[x\\u00d7]").matcher(text);

                if (xLeading.find()) {
                    qtyTotal = Integer.parseInt(xLeading.group(1));
                } else {
                    Matcher bareNumQty = Pattern.compile("\\b(\\d+)\\b\\s*(?:item|paket|pcs|pieces)").matcher(text);
                    if (bareNumQty.find()) {
                        qtyTotal = Integer.parseInt(bareNumQty.group(1));
                    }
                }
            }
        }

        if (qtyTotal != null && qtyTotal > 0) {
            results.put("qtyTotal", String.valueOf(qtyTotal));
        }
        if (qtyHot != null && qtyHot > 0) {
            results.put("qtyHot", String.valueOf(qtyHot));
        }


        // === Budget patterns
        // Interpretasi requirement (sesuai konfirmasi Anda):
        // - "3x21k" => qtyTotal=3, budgetPerItem=21000
        // - "budget 20k" => budgetPerItem=20000 (karena pola per item tidak selalu ada, kita pakai sebagai budgetPerItem)
        // - "total 63k" / "budget total" => budgetTotal=63000
        Long budgetPerItemRp = null;
        Long budgetTotalRp = null;

        // 1) explicit "<qty>x<price>k" / "<qty>x<price>" => per-item budget
        Matcher xBudget = Pattern.compile("\\b(\\d+)\\s*[x×]\\s*(\\d+(?:[.,]\\d+)?)\\s*[kK]?\\b").matcher(text);
        if (xBudget.find()) {
            int q = Integer.parseInt(xBudget.group(1));
            String priceNum = xBudget.group(2).replace(",", "").replace(".", "");
            long priceRp = Long.parseLong(priceNum);
            // if contains decimal or explicit k, treat as k-thousand; otherwise guess by magnitude
            boolean hadK = text.matches(".*(\\b" + q + "\\s*[x×]\\s*" + xBudget.group(2) + "\\s*[kK]).*");
            // Simpler: if original token had 'k', multiply; detect by searching around match
            String matchedChunk = xBudget.group(0);
            if (matchedChunk.contains("k") || matchedChunk.contains("K")) {
                priceRp = priceRp * 1000;
            }
            budgetPerItemRp = priceRp;
            if (qtyTotal == null || qtyTotal != q) {
                results.put("qtyTotal", String.valueOf(q));
            }
            results.put("budgetPerItem", String.valueOf(budgetPerItemRp));
        } else {
            // 2) "budget <num>k"
            Matcher budgetK = Pattern.compile("(?:budget|anggaran)\\s*(\\d+(?:[.,]\\d+)?)\\s*[kK]").matcher(text);
            if (budgetK.find()) {
                String num = budgetK.group(1).replace(",", "").replace(".", "");
                budgetPerItemRp = Long.parseLong(num) * 1000;
                results.put("budgetPerItem", String.valueOf(budgetPerItemRp));
            } else {
                // 3) "budget <full number>" (>=4 digits)
                Matcher budgetFull = Pattern.compile("(?:budget|anggaran)\\s*(\\d{4,})").matcher(text);
                if (budgetFull.find()) {
                    budgetPerItemRp = Long.parseLong(budgetFull.group(1));
                    results.put("budgetPerItem", String.valueOf(budgetPerItemRp));
                }
            }

            // 4) total budget: "total 63k" / "budget total 63k"
            Matcher totalK = Pattern.compile("(?:total(?:\\s*budget)?|budget\\s*total)\\s*(\\d+(?:[.,]\\d+)?)\\s*[kK]").matcher(text);
            if (totalK.find()) {
                String num = totalK.group(1).replace(",", "").replace(".", "");
                budgetTotalRp = Long.parseLong(num) * 1000;
                results.put("budgetTotal", String.valueOf(budgetTotalRp));
            } else {
                Matcher totalFull = Pattern.compile("(?:total(?:\\s*budget)?|budget\\s*total)\\s*(\\d{4,})").matcher(text);
                if (totalFull.find()) {
                    budgetTotalRp = Long.parseLong(totalFull.group(1));
                    results.put("budgetTotal", String.valueOf(budgetTotalRp));
                }
            }
        }

        // === Harga (legacy support "35k", "35000", "35.000", etc.) ===
        // Tetap dipertahankan untuk compatibility.
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

        // === Need detection (optional, used for need→solusi mapping later)
        // We just store raw need phrase keyword presence.
        // "need <something>" / "kebutuhan <something>"
        Matcher needMatcher = Pattern.compile("(?:need|kebutuhan)\\s*([^,\n]+)").matcher(text);
        if (needMatcher.find()) {
            results.put("need", needMatcher.group(1).trim());
        }

        return results;
    }
}


