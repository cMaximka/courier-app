package com.example.courierapp.data;

import com.example.courierapp.domain.entity.Client;
import com.example.courierapp.domain.entity.Courier;
import com.example.courierapp.domain.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {

    // Метод для регистрации клиента
    public boolean registerClient(Client client) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "INSERT INTO users (full_name, login, phone, password, user_type, address) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement userStmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, client.getFullName());
            userStmt.setString(2, client.getLogin());
            userStmt.setString(3, client.getPhone());
            userStmt.setString(4, client.getPassword());
            userStmt.setInt(5, 1); // 1 = client
            userStmt.setString(6, client.getAddress());

            int affectedRows = userStmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    long userId = generatedKeys.getLong(1);
                    client.setId(String.valueOf(userId));
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка регистрации клиента", e);
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для регистрации курьера
    public boolean registerCourier(Courier courier) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "INSERT INTO users (full_name, login, phone, password, user_type, passport_data, driver_license) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement userStmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, courier.getFullName());
            userStmt.setString(2, courier.getLogin());
            userStmt.setString(3, courier.getPhone());
            userStmt.setString(4, courier.getPassword());
            userStmt.setInt(5, 2); // 2 = courier
            userStmt.setString(6, courier.getPassportData());
            userStmt.setString(7, courier.getDriverLicense());

            int affectedRows = userStmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    long userId = generatedKeys.getLong(1);
                    courier.setId(String.valueOf(userId));
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка регистрации курьера", e);
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для входа в систему
    public User login(String login, String password) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return null;

        String query = "SELECT id, full_name, login, phone, password, user_type, address, passport_data, driver_license FROM users WHERE login = ? AND password = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, login);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("id");
                String fullName = rs.getString("full_name");
                String phone = rs.getString("phone");
                int userType = rs.getInt("user_type");

                if (userType == 1) { // client
                    String address = rs.getString("address");
                    Client client = new Client(fullName, login, phone, password, address);
                    client.setId(userId);
                    return client;

                } else if (userType == 2) { // courier
                    String passportData = rs.getString("passport_data");
                    String driverLicense = rs.getString("driver_license");
                    Courier courier = new Courier(fullName, login, phone, password, passportData, driverLicense);
                    courier.setId(userId);
                    return courier;
                }
            }

            return null;

        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка входа", e);
            return null;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Проверка существования логина
    public boolean isLoginExists(String login) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "SELECT 1 FROM users WHERE login = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка проверки логина", e);
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}