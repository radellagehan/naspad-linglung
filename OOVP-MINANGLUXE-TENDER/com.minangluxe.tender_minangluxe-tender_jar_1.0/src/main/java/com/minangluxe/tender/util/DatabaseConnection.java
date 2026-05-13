package com.minangluxe.tender.util;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/minangluxe_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 
    private static Connection connection = null;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                initializeDatabase(connection);
            }
        } catch (Exception e) {
            System.err.println("❌ Database Connection Failed!");
            e.printStackTrace();
        }
        return connection;
    }

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Health check: "Table doesn't exist in engine" errors often require a DROP and RECREATE
            boolean needsRepair = false;
            try {
                // Try a simple query to see if the table is actually accessible
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM users LIMIT 1");
                rs.close();
            } catch (SQLException e) {
                // If error contains "doesn't exist" or "engine", we force a repair
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("doesn't exist") || msg.contains("engine") || msg.contains("not found")) {
                    needsRepair = true;
                    System.err.println("⚠️ Database corruption detected: " + e.getMessage());
                }
            }

            if (needsRepair) {
                System.out.println("🔧 Attempting to repair database by recreating tables...");
                stmt.executeUpdate("DROP TABLE IF EXISTS tender_offers");
                stmt.executeUpdate("DROP TABLE IF EXISTS tender_requests");
                stmt.executeUpdate("DROP TABLE IF EXISTS tenders");
                stmt.executeUpdate("DROP TABLE IF EXISTS users");
            }

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "phone VARCHAR(20))");
            
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tenders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "paket VARCHAR(100), " +
                    "berat VARCHAR(50), " +
                    "budget VARCHAR(50))");
            
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tender_requests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "buyer_id INT NOT NULL, " +
                    "query VARCHAR(500) NOT NULL, " +
                    "address VARCHAR(300), " +
                    "status VARCHAR(20) DEFAULT 'OPEN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)" );

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tender_offers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "request_id INT NOT NULL, " +
                    "seller_name VARCHAR(100), " +
                    "product_name VARCHAR(200), " +
                    "price DOUBLE, " +
                    "quantity INT, " +
                    "rating DOUBLE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tender_negotiations (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "request_id INT NOT NULL, " +
                    "offer_id INT NOT NULL, " +
                    "seller_name VARCHAR(100), " +
                    "buyer_id INT, " +
                    "buyer_price DOUBLE, " +
                    "seller_price DOUBLE, " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Seed default users if table is empty
            ResultSet rsCount = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rsCount.next() && rsCount.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO users (username, password, role) VALUES ('admin', 'admin', 'SELLER')");
                stmt.executeUpdate("INSERT INTO users (username, password, role) VALUES ('buyer', 'buyer', 'BUYER')");
                stmt.executeUpdate("INSERT INTO users (username, password, role) VALUES ('seller', 'seller', 'SELLER')");
                System.out.println("✅ Default users created: admin, buyer, seller");
            }
            rsCount.close();

        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize/repair tables: " + e.getMessage());
        }
    }
    
    public static int executeUpdate(String query, Object... params) {
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}