package com.minangluxe;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@Controller
public class SellerController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/seller")
    public String dashboard(Model model) {
        List<Map<String, Object>> triggers = jdbcTemplate.queryForList(
            "SELECT * FROM active_triggers WHERE status = 'waiting' ORDER BY id DESC");
        model.addAttribute("triggers", triggers);
        return "seller";
    }

    @PostMapping("/seller/respond")
    public String respond(@RequestParam("triggerId") int triggerId, 
                          @RequestParam("keyword") String keyword,
                          @RequestParam(value="s1", required=false, defaultValue="") String s1, 
                          @RequestParam(value="s2", required=false, defaultValue="") String s2, 
                          @RequestParam(value="s3", required=false, defaultValue="") String s3) {
        try {
            // Gunakan ON DUPLICATE KEY UPDATE agar tidak error jika keyword sudah ada
            String sql = "INSERT INTO keyword_settings (keyword, input1_name, input2_name, input3_name) " +
                         "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE input1_name=?, input2_name=?, input3_name=?";
            
            jdbcTemplate.update(sql, keyword, s1, s2, s3, s1, s2, s3);
            jdbcTemplate.update("UPDATE active_triggers SET status = 'processed' WHERE id = ?", triggerId);
            System.out.println("✅ Success: Recommended " + s1 + " for " + keyword);
        } catch (DataAccessException e) {
            System.err.println("❌ Database Error: " + e.getMessage());
        }
        return "redirect:/seller";
    }

    @PostMapping("/seller/reset")
    public String reset() {
        jdbcTemplate.update("DELETE FROM active_triggers");
        jdbcTemplate.update("DELETE FROM keyword_settings");
        return "redirect:/seller";
    }
}