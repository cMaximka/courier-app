package com.example.courierapp.data;

import com.example.courierapp.domain.entity.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    // Создание заказа
    public boolean createOrder(Order order) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "INSERT INTO orders (client_id, pickup_address, delivery_address, status, price, weight, length, width, height, product_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, order.getClientId());
            stmt.setString(2, order.getPickupAddress());
            stmt.setString(3, order.getDeliveryAddress());
            stmt.setInt(4, order.getStatus());
            stmt.setDouble(5, order.getPrice());
            stmt.setDouble(6, order.getWeight());
            stmt.setDouble(7, order.getLength());
            stmt.setDouble(8, order.getWidth());
            stmt.setDouble(9, order.getHeight());
            stmt.setDouble(10, order.getProductPrice());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    order.setId(String.valueOf(generatedKeys.getLong(1)));
                    return true;
                }
            }
            return false;

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка создания заказа", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Получение заказов клиента
    public List<Order> getOrdersByClientId(String clientId) {
        Connection conn = DBWorker.getConnection();
        List<Order> orders = new ArrayList<>();
        if (conn == null) return orders;

        String query = "SELECT o.*, " +
                "c.full_name as client_name, c.phone as client_phone, " +
                "cr.full_name as courier_name, cr.phone as courier_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "LEFT JOIN users cr ON o.courier_id = cr.id " +
                "WHERE o.client_id = ? ORDER BY o.created_at DESC";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                orders.add(order);
            }

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка получения заказов клиента", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return orders;
    }

    // Получение всех доступных заказов
    public List<Order> getAllAvailableOrders() {
        Connection conn = DBWorker.getConnection();
        List<Order> orders = new ArrayList<>();
        if (conn == null) return orders;

        String query = "SELECT o.*, " +
                "c.full_name as client_name, c.phone as client_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "WHERE o.status = 1 ORDER BY o.created_at DESC";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                orders.add(order);
            }

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка получения доступных заказов", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return orders;
    }

    // Получение заказов курьера (статусы 2, 3, 4 - все принятые)
    public List<Order> getOrdersByCourierId(String courierId) {
        Connection conn = DBWorker.getConnection();
        List<Order> orders = new ArrayList<>();
        if (conn == null) return orders;

        String query = "SELECT o.*, " +
                "c.full_name as client_name, c.phone as client_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "WHERE o.courier_id = ? AND o.status IN (2, 3, 4) ORDER BY o.created_at DESC";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, courierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = createOrderFromResultSet(rs);
                orders.add(order);
            }

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка получения заказов курьера", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return orders;
    }

    // Принятие заказа со списанием баланса
    public boolean acceptOrderWithPayment(String orderId, String courierId, double price) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        double deposit = price / 2;

        try {
            conn.setAutoCommit(false);

            String checkBalanceQuery = "SELECT balance FROM users WHERE id = ? FOR UPDATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkBalanceQuery);
            checkStmt.setString(1, courierId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return false;
            }

            double currentBalance = rs.getDouble("balance");
            if (currentBalance < deposit) {
                conn.rollback();
                android.util.Log.e("ORDER_REPO", "Недостаточно средств. Нужно: " + deposit + ", доступно: " + currentBalance);
                return false;
            }

            String updateBalanceQuery = "UPDATE users SET balance = balance - ? WHERE id = ?";
            PreparedStatement balanceStmt = conn.prepareStatement(updateBalanceQuery);
            balanceStmt.setDouble(1, deposit);
            balanceStmt.setString(2, courierId);
            balanceStmt.executeUpdate();

            String updateOrderQuery = "UPDATE orders SET courier_id = ?, status = 2 WHERE id = ? AND status = 1";
            PreparedStatement orderStmt = conn.prepareStatement(updateOrderQuery);
            orderStmt.setString(1, courierId);
            orderStmt.setString(2, orderId);
            int affectedRows = orderStmt.executeUpdate();

            if (affectedRows > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка принятия заказа", e);
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Отмена заказа (без возврата средств)
    public boolean cancelOrder(String orderId) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "UPDATE orders SET courier_id = NULL, status = 1 WHERE id = ? AND status = 2";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, orderId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка отмены заказа", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Обновление статуса заказа
    public boolean updateOrderStatus(String orderId, int newStatus) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "UPDATE orders SET status = ? WHERE id = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, newStatus);
            stmt.setString(2, orderId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка обновления статуса заказа", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Подтверждение клиентом (отдал заказ)
    public boolean confirmDeliveredByClient(String orderId) {
        Connection conn = DBWorker.getConnection();
        if (conn == null) return false;

        String query = "UPDATE orders SET status = 4 WHERE id = ? AND status = 3";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, orderId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            android.util.Log.e("ORDER_REPO", "Ошибка подтверждения получения заказа клиентом", e);
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private Order createOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order(
                rs.getString("client_id"),
                rs.getString("pickup_address"),
                rs.getString("delivery_address"),
                0, 0, 0, 0, 0
        );
        order.setId(rs.getString("id"));
        order.setStatus(rs.getInt("status"));
        order.setPrice(rs.getDouble("price"));
        order.setCreatedAt(rs.getString("created_at"));
        order.setClientName(rs.getString("client_name"));
        order.setClientPhone(rs.getString("client_phone"));

        try {
            String courierName = rs.getString("courier_name");
            String courierPhone = rs.getString("courier_phone");
            if (courierName != null) {
                order.setCourierName(courierName);
                order.setCourierPhone(courierPhone);
            }
        } catch (SQLException e) { }

        String courierId = rs.getString("courier_id");
        if (courierId != null) {
            order.setCourierId(courierId);
        }

        return order;
    }
}