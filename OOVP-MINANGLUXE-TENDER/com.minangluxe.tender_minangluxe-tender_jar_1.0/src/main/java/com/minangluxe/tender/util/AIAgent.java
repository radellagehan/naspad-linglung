package com.minangluxe.tender.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AIAgent {

    private static final String API_KEY = getApiKey();
    private static final String API_URL = "xai-cKSV5AogTMMtyJArmHYGSxm7ZDNb4vpLJNypu8gnPJdibyJ2J5uXZzJZHTckQI3wPiHJrZN7cuY4K16w";
    private static final String MODEL = "grok-beta";   // You can change to "grok-2" later

    private static String getApiKey() {
        // 1. From Environment Variable (Best for production)
        String key = System.getenv("XAI_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key.trim();
        }

        // 2. From .env file (Good for development)
        try {
            File envFile = new File(".env");
            if (envFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("XAI_API_KEY=")) {
                            return line.substring(12).trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // silent
        }

        System.err.println("❌ [AIAgent] XAI_API_KEY not found!");
        System.err.println("   Please create .env file or set environment variable.");
        return null;
    }

    public Map<String, String> analyzeText(String userText) {
        Map<String, String> result = new HashMap<>();

        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("⚠️ AI Disabled - No API Key");
            return getFallbackResult(userText);
        }

        System.out.println("🤖 Grok AI analyzing: " + userText);

        try {
            String jsonResponse = callGrokAPI(userText);
            result = parseAIResponse(jsonResponse);

            // If AI didn't return useful data, combine with local parser
            if (result.isEmpty()) {
                result.putAll(OrderParser.parseToMap(userText));
            }

        } catch (Exception e) {
            System.err.println("❌ AI Request failed: " + e.getMessage());
            return getFallbackResult(userText);
        }

        return result.isEmpty() ? getFallbackResult(userText) : result;
    }

    private String callGrokAPI(String userPrompt) throws Exception {
        String systemPrompt = """
            You are a smart assistant for MinangLuxe Tender System.
            Extract order information and return ONLY valid JSON with these keys:
            - paket (string): S1, S2, S3, or B
            - berat (string): number in gram
            - harga (string): number in Rupiah (full number, not "35k")

            If not clear, make your best guess.
            """;

        String body = """
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"}
                ],
                "temperature": 0.3,
                "max_tokens": 300
            }
            """.formatted(MODEL, systemPrompt.replace("\"", "\\\""), 
                          userPrompt.replace("\"", "\\\"").replace("\n", "\\n"));

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("utf-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            System.err.println("API Error Code: " + responseCode);
            throw new RuntimeException("HTTP " + responseCode);
        }
    }

    private Map<String, String> parseAIResponse(String jsonResponse) {
        Map<String, String> map = new HashMap<>();

        try {
            // Simple but effective extraction
            if (jsonResponse.contains("\"paket\"")) {
                map.put("paket", extractValue(jsonResponse, "paket"));
            }
            if (jsonResponse.contains("\"berat\"")) {
                map.put("berat", extractValue(jsonResponse, "berat"));
            }
            if (jsonResponse.contains("\"harga\"")) {
                map.put("harga", extractValue(jsonResponse, "harga"));
            }
        } catch (Exception e) {
            // ignore
        }

        return map;
    }

    private String extractValue(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return "";

            start = json.indexOf(":", start) + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf("\"", start);
                return json.substring(start, end).trim();
            } else {
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                return json.substring(start, end).trim().replace("\"", "");
            }
        } catch (Exception e) {
            return "";
        }
    }

    private Map<String, String> getFallbackResult(String originalText) {
        Map<String, String> map = OrderParser.parseToMap(originalText);
        if (map.isEmpty()) {
            map.put("paket", "S2");
            map.put("berat", "500");
            map.put("harga", "25000");
        }
        return map;
    }
}