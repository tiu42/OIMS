package com.oims.core.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBConnection {
    private static String URL = "";
    private static String USER = "";
    private static String PASSWORD = "";

    static {
        Map<String, String> env = loadEnv();
        if (env.containsKey("DB_URL")) {
            URL = env.get("DB_URL");
        }
        if (env.containsKey("DB_USER")) {
            USER = env.get("DB_USER");
        }
        if (env.containsKey("DB_PASSWORD")) {
            PASSWORD = env.get("DB_PASSWORD");
        }
    }

    private static Map<String, String> loadEnv() {
        Map<String, String> env = new HashMap<>();
        
        // 1. Try reading system environment variables
        String[] keys = {"DB_URL", "DB_USER", "DB_PASSWORD"};
        for (String key : keys) {
            String val = System.getenv(key);
            if (val != null && !val.trim().isEmpty()) {
                env.put(key, val.trim());
            }
        }
        
        // 2. Try reading .env file (local)
        File envFile = new File(".env");
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if ((value.startsWith("\"") && value.endsWith("\"")) || 
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        // System env vars take precedence over .env file
                        if (!env.containsKey(key)) {
                            env.put(key, value.trim());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading .env file: " + e.getMessage());
            }
        }
        return env;
    }

    public static Connection getConnection() throws SQLException {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e){
            throw new SQLException("Khong tim thay MySQL Driver!", e);
        }
    }
}
