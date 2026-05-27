package com.example.courierapp.data;

import com.example.courierapp.domain.entity.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    public boolean createOrder(Order order) {
        String sql = "INSERT INTO orders (client_id, pickup_address, delivery_address, " +
                "weight, length, width, height, product_price, price, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBWorker.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, order.getClientId());
            pstmt.setString(2, order.getPickupAddress());
            pstmt.setString(3, order.getDeliveryAddress());
            pstmt.setDouble(4, order.getWeight());
            pstmt.setDouble(5, order.getLength());
            pstmt.setDouble(6, order.getWidth());
            pstmt.setDouble(7, order.getHeight());
            pstmt.setDouble(8, order.getProductPrice());
            pstmt.setDouble(9, order.getPrice());
            pstmt.setInt(10, order.getStatus());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    order.setId(String.valueOf(rs.getLong(1))); // Преобразуем числовой ID в строку
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Order> getActiveOrdersByClientId(String clientId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.client_id, o.courier_id, o.pickup_address, o.delivery_address, " +
                "o.status, o.price, o.created_at, " +
                "o.weight, o.length, o.width, o.height, o.product_price, " +
                "c.full_name AS client_name, c.phone AS client_phone, " +
                "cou.full_name AS courier_name, cou.phone AS courier_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "LEFT JOIN users cou ON o.courier_id = cou.id " +
                "WHERE o.client_id = ? AND o.status NOT IN (5,6) ORDER BY o.created_at DESC";

        try (Connection conn = DBWorker.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(mapOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getCompletedOrdersByCourierId(String courierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.client_id, o.courier_id, o.pickup_address, o.delivery_address, " +
                "o.status, o.price, o.created_at, " +
                "o.weight, o.length, o.width, o.height, o.product_price, " +
                "c.full_name AS client_name, c.phone AS client_phone, " +
                "cou.full_name AS courier_name, cou.phone AS courier_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "LEFT JOIN users cou ON o.courier_id = cou.id " +
                "WHERE o.courier_id = ? AND o.status = 5 ORDER BY o.created_at DESC";

        try (Connection conn = DBWorker.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courierId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(mapOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getAllAvailableOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.client_id, o.courier_id, o.pickup_address, o.delivery_address, " +
                "o.status, o.price, o.created_at, " +
                "o.weight, o.length, o.width, o.height, o.product_price, " +
                "c.full_name AS client_name, c.phone AS client_phone, " +
                "cou.full_name AS courier_name, cou.phone AS courier_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "LEFT JOIN users cou ON o.courier_id = cou.id " +
                "WHERE o.status = 1 AND o.courier_id IS NULL " +
                "ORDER BY o.created_at ASC";

        try (Connection conn = DBWorker.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = mapOrderFromResultSet(rs);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByCourierId(String courierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.client_id, o.courier_id, o.pickup_address, o.delivery_address, " +
                "o.status, o.price, o.created_at, " +
                "o.weight, o.length, o.width, o.height, o.product_price, " +
                "c.full_name AS client_name, c.phone AS client_phone, " +
                "cou.full_name AS courier_name, cou.phone AS courier_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "LEFT JOIN users cou ON o.courier_id = cou.id " +
                "WHERE o.courier_id = ? AND o.status NOT IN (5,6) ORDER BY o.created_at DESC";

        try (Connection conn = DBWorker.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courierId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = mapOrderFromResultSet(rs);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean acceptOrderWithPayment(String orderId, String courierId, double deposit) {
        String deductSql = "UPDATE users SET balance = balance - ? WHERE id = ?";
        String updateOrderSql = "UPDATE orders SET courier_id = ?, status = 2 WHERE id = ? AND status = 1";
        try (Connection conn = DBWorker.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deductStmt = conn.prepareStatement(deductSql)) {
                deductStmt.setDouble(1, deposit);
                deductStmt.setString(2, courierId);
                if (deductStmt.executeUpdate() == 0) throw new SQLException();
            }
            try (PreparedStatement orderStmt = conn.prepareStatement(updateOrderSql)) {
                orderStmt.setString(1, courierId);
                orderStmt.setString(2, orderId);
                if (orderStmt.executeUpdate() == 0) throw new SQLException();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Order> getCompletedOrdersByClientId(String clientId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.client_id, o.courier_id, o.pickup_address, o.delivery_address, " +
                "o.status, o.price, o.created_at, " +
                "o.weight, o.length, o.width, o.height, o.product_price, " +
                "c.full_name AS client_name, c.phone AS client_phone, " +
                "cou.full_name AS courier_name, cou.phone AS courier_phone " +
                "FROM orders o " +
                "LEFT JOIN users c ON o.client_id = c.id " +
                "LEFT JOIN users cou ON o.courier_id = cou.id " +
                "WHERE o.client_id = ? AND o.status = 5 ORDER BY o.created_at DESC";

        try (Connection conn = DBWorker.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(mapOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean completeOrderWithPayment(String orderId, String clientId, String courierId, double price) {
        String deductFromClientSql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String addToCourierSql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        String updateOrderSql = "UPDATE orders SET status = 5 WHERE id = ? AND status = 4";

        try (Connection conn = DBWorker.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deductStmt = conn.prepareStatement(deductFromClientSql)) {
                deductStmt.setDouble(1, price);
                deductStmt.setString(2, clientId);
                deductStmt.setDouble(3, price);
                int affected = deductStmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement addStmt = conn.prepareStatement(addToCourierSql)) {
                addStmt.setDouble(1, price);
                addStmt.setString(2, courierId);
                addStmt.executeUpdate();
            }

            try (PreparedStatement orderStmt = conn.prepareStatement(updateOrderSql)) {
                orderStmt.setString(1, orderId);
                int affected = orderStmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelOrderByCourier(String orderId) {
        String selectSql = "SELECT courier_id, status FROM orders WHERE id = ?";
        String updateOrderSql = "UPDATE orders SET courier_id = NULL, status = 1 WHERE id = ?";

        try (Connection conn = DBWorker.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, orderId);
                ResultSet rs = selectStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                int status = rs.getInt("status");

                if (status != 2 && status != 3) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateOrderSql)) {
                    updateStmt.setString(1, orderId);
                    if (updateStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Отмена заказа клиентом: статус меняется на 6, залог возвращается курьеру
    public boolean cancelOrderByClient(String orderId) {
        String selectSql = "SELECT courier_id, price, status FROM orders WHERE id = ?";
        String updateOrderSql = "UPDATE orders SET status = 6 WHERE id = ?";
        String refundCourierSql = "UPDATE users SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DBWorker.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, orderId);
                ResultSet rs = selectStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                String courierId = rs.getString("courier_id");
                double price = rs.getDouble("price");
                int status = rs.getInt("status");

                // Нельзя отменить завершённый заказ или уже отменённый
                if (status == 5 || status == 6) {
                    conn.rollback();
                    return false;
                }

                // Если у заказа есть курьер, вернуть ему залог
                if (courierId != null && !courierId.isEmpty()) {
                    double deposit = price / 2.0;
                    try (PreparedStatement refundStmt = conn.prepareStatement(refundCourierSql)) {
                        refundStmt.setDouble(1, deposit);
                        refundStmt.setString(2, courierId);
                        if (refundStmt.executeUpdate() == 0) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateOrderSql)) {
                    updateStmt.setString(1, orderId);
                    if (updateStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET pickup_address = ?, delivery_address = ?, " +
            "weight = ?, length = ?, width = ?, height = ?, product_price = ?, price = ? " +
            "WHERE id = ? AND status IN (1,2)"; // allow editing until courier has picked up (status < 3)
        try (Connection conn = DBWorker.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, order.getPickupAddress());
            pstmt.setString(2, order.getDeliveryAddress());
            pstmt.setDouble(3, order.getWeight());
            pstmt.setDouble(4, order.getLength());
            pstmt.setDouble(5, order.getWidth());
            pstmt.setDouble(6, order.getHeight());
            pstmt.setDouble(7, order.getProductPrice());
            pstmt.setDouble(8, order.getPrice());
            pstmt.setString(9, order.getId());

            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrderStatus(String orderId, int newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBWorker.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newStatus);
            pstmt.setString(2, orderId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean confirmDeliveredByClient(String orderId) {
        return updateOrderStatus(orderId, 4);
    }
    public boolean completeOrder(String orderId) {
        return updateOrderStatus(orderId, 5);
    }

    private Order mapOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order(
                rs.getString("client_id"),
                rs.getString("pickup_address"),
                rs.getString("delivery_address"),
                rs.getDouble("weight"),
                rs.getDouble("length"),
                rs.getDouble("width"),
                rs.getDouble("height"),
                rs.getDouble("product_price")
        );
        order.setId(rs.getString("id"));
        order.setCourierId(rs.getString("courier_id"));
        order.setStatus(rs.getInt("status"));
        order.setPrice(rs.getDouble("price"));
        order.setCreatedAt(rs.getString("created_at"));

        order.setClientName(rs.getString("client_name"));
        order.setClientPhone(rs.getString("client_phone"));
        order.setCourierName(rs.getString("courier_name"));
        order.setCourierPhone(rs.getString("courier_phone"));

        return order;
    }

    public boolean completeOrderTransaction(String orderId, String clientId, String courierId, double amount) {
        String deductClient = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String addCourier = "UPDATE users SET balance = balance + ? WHERE id = ?";
        String updateOrder = "UPDATE orders SET status = 5 WHERE id = ? AND status = 4";
        try (Connection conn = DBWorker.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(deductClient)) {
                ps.setDouble(1, amount);
                ps.setString(2, clientId);
                ps.setDouble(3, amount);
                if (ps.executeUpdate() == 0) throw new SQLException();
            }
            try (PreparedStatement ps = conn.prepareStatement(addCourier)) {
                ps.setDouble(1, amount);
                ps.setString(2, courierId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateOrder)) {
                ps.setString(1, orderId);
                if (ps.executeUpdate() == 0) throw new SQLException();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean confirmDeliveryByClient(String orderId, String clientId, String courierId, double amount) {
    String checkSql = "SELECT status FROM orders WHERE id = ? AND client_id = ? AND status = 7";
    String deductClient = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
    String addCourier = "UPDATE users SET balance = balance + ? WHERE id = ?";
    String updateOrder = "UPDATE orders SET status = 5 WHERE id = ? AND status = 7";
    try (Connection conn = DBWorker.getConnection()) {
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, orderId);
            ps.setString(2, clientId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                conn.rollback();
                return false;
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(deductClient)) {
            ps.setDouble(1, amount);
            ps.setString(2, clientId);
            ps.setDouble(3, amount);
            if (ps.executeUpdate() == 0) throw new SQLException("Недостаточно средств у клиента");
        }

        try (PreparedStatement ps = conn.prepareStatement(addCourier)) {
            ps.setDouble(1, amount);
            ps.setString(2, courierId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(updateOrder)) {
            ps.setString(1, orderId);
            if (ps.executeUpdate() == 0) throw new SQLException();
        }
        conn.commit();
        return true;
        
        } catch (SQLException e) {
        e.printStackTrace();
            return false;
        }
    }
}