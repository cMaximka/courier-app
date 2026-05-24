package com.example.courierapp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBWorker {
    private static final String URL = "jdbc:mysql://81.177.165.129:3306/j86805296_delivery";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, "j86805296", "=pt3f4fk=432-kD");
            android.util.Log.d("DB_SUCCESS", "Подключено к БД");
        } catch (SQLException e) {
            android.util.Log.e("DB_ERROR", "Ошибка: " + e.getMessage(), e);
        }
        return connection;
    }
}
