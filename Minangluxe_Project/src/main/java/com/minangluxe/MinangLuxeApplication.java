package com.minangluxe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@Controller
public class MinangLuxeApplication {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Bagian AI ini dimatikan dulu supaya nggak BUILD FAILED
    // @Autowired(required = false)
    // private OllamaChatModel chatModel;

    public static void main(String[] args) {
        SpringApplication.run(MinangLuxeApplication.class, args);
    }

    @GetMapping("/")
public String index(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
    // 1. Ambil semua data dari database (sudah urut termurah karena ORDER BY harga ASC)
    List<Map<String, Object>> allMenu = jdbcTemplate.queryForList("SELECT * FROM menu ORDER BY harga ASC");
    List<Map<String, Object>> displayMenu = new ArrayList<>(allMenu);
    
    String aiStatus = null;
    Map<String, Object> cheapestDeal = null;

    // 2. Logika Smart Filter (Bisa baca "I want to order nasi padang")
    if (keyword != null && !keyword.trim().isEmpty()) {
        String[] words = keyword.toLowerCase().split("\\s+");
        
        displayMenu = allMenu.stream()
            .filter(m -> {
                String namaMenu = m.get("nama_menu").toString().toLowerCase();
                for (String word : words) {
                    if (word.length() > 2 && namaMenu.contains(word)) return true;
                }
                return false;
            })
            .toList();
        
        aiStatus = "Smart NLP Filter Active";
    }

    // 3. Ambil menu paling murah dari hasil pencarian untuk dijadikan "Highlight"
    if (!displayMenu.isEmpty()) {
        cheapestDeal = displayMenu.get(0); // Index 0 pasti termurah karena sudah di-sort di SQL
    }

    model.addAttribute("menuList", displayMenu);
    model.addAttribute("cheapestDeal", cheapestDeal); // Kirim data menu termurah ke HTML
    model.addAttribute("keyword", keyword);
    model.addAttribute("aiStatus", aiStatus);
    
    return "index";
    }
}

