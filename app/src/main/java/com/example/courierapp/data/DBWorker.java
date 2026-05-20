package com.example.courierapp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBWorker {
    private static final String URL = "jdbc:mysql://192.168.50.140:3306/delivery";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(URL, "root", "");
            android.util.Log.d("DB_SUCCESS", "Подключено к БД: ");
        } catch (ClassNotFoundException e) {
            android.util.Log.e("DB_ERROR", "Драйвер не найден", e);
        } catch (SQLException e) {
            android.util.Log.e("DB_ERROR", "Ошибка: " + e.getMessage(), e);
        }
        return connection;
    }
}
