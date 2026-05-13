package com.minangluxe.tender;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.minangluxe.tender.view.LoginFrame;
import com.minangluxe.tender.util.DatabaseConnection;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Inisialisasi database saat aplikasi berjalan
        DatabaseConnection.getConnection();
        
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}