package com.minangluxe;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class MenuService {
    public List<Menu> filterMenu(List<Menu> allMenu, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return allMenu;
        String k = keyword.toLowerCase();
        return allMenu.stream().filter(m -> {
            String name = m.getNamaMenu().toLowerCase();
            int price = m.getHarga();
            if (k.contains("murah")) return price <= 15000;
            if (k.contains("mahal")) return price >= 25000;
            if (k.contains("pedas")) return name.contains("balado") || name.contains("rendang");
            return name.contains(k);
        }).collect(Collectors.toList());
    }
}