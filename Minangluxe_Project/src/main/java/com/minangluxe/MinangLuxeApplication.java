package com.minangluxe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@Controller
public class MinangLuxeApplication {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private MenuService menuService;

    public static void main(String[] args) {
        SpringApplication.run(MinangLuxeApplication.class, args);
    }

    // --- BUYER CONTROLLER ---
    @GetMapping("/")
    public String index(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM menu");
        List<Menu> allMenu = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            allMenu.add(new Menu((int)r.get("id"), (String)r.get("nama_menu"), (int)r.get("harga")));
        }

        List<Menu> filteredMenu = menuService.filterMenu(allMenu, keyword);
        model.addAttribute("menuList", filteredMenu);

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("keyword", keyword);
            jdbcTemplate.update("INSERT IGNORE INTO active_triggers (keyword, status) VALUES (?, 'waiting')", keyword);
            
            List<Map<String, Object>> settings = jdbcTemplate.queryForList("SELECT * FROM keyword_settings WHERE keyword = ?", keyword);
            if (!settings.isEmpty()) {
                Map<String, Object> s = settings.get(0);
                List<String> recs = new ArrayList<>();
                if (s.get("input1_name") != null && !s.get("input1_name").toString().isEmpty()) recs.add(s.get("input1_name").toString());
                if (s.get("input2_name") != null && !s.get("input2_name").toString().isEmpty()) recs.add(s.get("input2_name").toString());
                if (s.get("input3_name") != null && !s.get("input3_name").toString().isEmpty()) recs.add(s.get("input3_name").toString());
                model.addAttribute("customRecs", recs);
                model.addAttribute("aiStatus", "Hasil khusus pencarian: " + keyword);
            } else {
                model.addAttribute("aiStatus", "Chef sedang menyiapkan rekomendasi...");
            }
        }
        return "index";
    }

    // --- SELLER CONTROLLER ---
    @GetMapping("/seller")
    public String seller(Model model) {
        model.addAttribute("triggers", jdbcTemplate.queryForList("SELECT * FROM active_triggers WHERE status = 'waiting'"));
        return "seller";
    }

    @PostMapping("/seller/respond")
    public String respond(@RequestParam int triggerId, @RequestParam String keyword, @RequestParam String s1, @RequestParam String s2, @RequestParam String s3) {
        jdbcTemplate.update("INSERT INTO keyword_settings (keyword, input1_name, input2_name, input3_name) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE input1_name=?, input2_name=?, input3_name=?", keyword, s1, s2, s3, s1, s2, s3);
        jdbcTemplate.update("UPDATE active_triggers SET status = 'processed' WHERE id = ?", triggerId);
        return "redirect:/seller";
    }

    @PostMapping("/order/finalize")
    public String finalize(@RequestParam Map<String, String> params, Model model) {
        Map<String, String> orders = new HashMap<>();
        params.forEach((k, v) -> { if (!k.equals("keyword") && v.matches("^[1-9]\\d*$")) orders.put(k, v); });
        model.addAttribute("orders", orders);
        return "summary";
    }
    @PostMapping("/seller/clear")
public String clearTriggers() {
    jdbcTemplate.update("DELETE FROM active_triggers");
    return "redirect:/seller";
}
}