package com.example.courierapp.data;

import com.example.courierapp.domain.entity.Client;
import com.example.courierapp.domain.entity.Courier;
import com.example.courierapp.domain.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {

    public boolean registerClient(Client client) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "INSERT INTO users (full_name, login, phone, password, user_type, address, balance) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement userStmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, client.getFullName());
            userStmt.setString(2, client.getLogin());
            userStmt.setString(3, client.getPhone());
            userStmt.setString(4, client.getPassword());
            userStmt.setInt(5, 1);
            userStmt.setString(6, client.getAddress());
            userStmt.setDouble(7, client.getBalance());

            int affectedRows = userStmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    client.setId(String.valueOf(generatedKeys.getLong(1)));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка регистрации клиента", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean registerCourier(Courier courier) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "INSERT INTO users (full_name, login, phone, password, user_type, passport_data, driver_license, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement userStmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, courier.getFullName());
            userStmt.setString(2, courier.getLogin());
            userStmt.setString(3, courier.getPhone());
            userStmt.setString(4, courier.getPassword());
            userStmt.setInt(5, 2);
            userStmt.setString(6, courier.getPassportData());
            userStmt.setString(7, courier.getDriverLicense());
            userStmt.setDouble(8, courier.getBalance());

            int affectedRows = userStmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    courier.setId(String.valueOf(generatedKeys.getLong(1)));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка регистрации курьера", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public User login(String login, String password) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return null;

        String query = "SELECT id, full_name, login, phone, password, user_type, address, passport_data, driver_license, balance FROM users WHERE login = ? AND password = ?";

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
                double balance = rs.getDouble("balance");

                if (userType == 1) {
                    String address = rs.getString("address");
                    Client client = new Client(fullName, login, phone, password, address);
                    client.setId(userId);
                    client.setBalance(balance);
                    return client;
                } else if (userType == 2) {
                    String passportData = rs.getString("passport_data");
                    String driverLicense = rs.getString("driver_license");
                    Courier courier = new Courier(fullName, login, phone, password, passportData, driverLicense);
                    courier.setId(userId);
                    courier.setBalance(balance);
                    return courier;
                }
            }
            return null;
        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка входа", e);
            return null;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public User getUserById(String userId) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return null;

        String query = "SELECT id, full_name, login, phone, password, user_type, address, passport_data, driver_license, balance FROM users WHERE id = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String login = rs.getString("login");
                String phone = rs.getString("phone");
                int userType = rs.getInt("user_type");
                double balance = rs.getDouble("balance");

                if (userType == 1) {
                    String address = rs.getString("address");
                    Client client = new Client(fullName, login, phone, "", address);
                    client.setId(userId);
                    client.setBalance(balance);
                    return client;
                } else if (userType == 2) {
                    String passportData = rs.getString("passport_data");
                    String driverLicense = rs.getString("driver_license");
                    Courier courier = new Courier(fullName, login, phone, "", passportData, driverLicense);
                    courier.setId(userId);
                    courier.setBalance(balance);
                    return courier;
                }
            }
            return null;
        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка получения пользователя", e);
            return null;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean updateProfile(User user) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query;
        if (user.getUserType() == 1) {
            query = "UPDATE users SET full_name = ?, phone = ?, address = ? WHERE id = ?";
        } else {
            query = "UPDATE users SET full_name = ?, phone = ?, passport_data = ?, driver_license = ? WHERE id = ?";
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhone());

            if (user.getUserType() == 1) {
                Client client = (Client) user;
                stmt.setString(3, client.getAddress());
                stmt.setString(4, user.getId());
            } else {
                Courier courier = (Courier) user;
                stmt.setString(3, courier.getPassportData());
                stmt.setString(4, courier.getDriverLicense());
                stmt.setString(5, user.getId());
            }

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка обновления профиля", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean addBalance(String userId, double amount) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "UPDATE users SET balance = balance + ? WHERE id = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, amount);
            stmt.setString(2, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            android.util.Log.e("AUTH_REPO", "Ошибка пополнения баланса", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

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
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}