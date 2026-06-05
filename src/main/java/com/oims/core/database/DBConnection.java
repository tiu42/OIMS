package com.oims.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/glocerimex";
    private static final String USER = "root";
    private static final String PASSWORD = "quangminh061104";

    public static Connection getConnection() throws SQLException {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e){
            throw new SQLException("Khong tim thay MySQL Driver!", e);
        }
    }
}
