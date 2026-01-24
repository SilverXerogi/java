package bookstore.dao;

import bookstore.jdbc.ConnectionManager;
import bookstore.jdbc.TransactionalExecutor;
import bookstore.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO implements GenericDAO<Order, Integer> {

    @Override
    public Order findById(Integer id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToOrder(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске заказа", e);
        }
        return null;
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех заказов", e);
        }
        return orders;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении заказа", e);
        }
    }

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order(
                rs.getInt("id"),
                new HashMap<>(),
                rs.getString("customer_name"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
        order.setStatus(Order.Status.valueOf(rs.getString("status")));
        order.setTotalPrice(rs.getBigDecimal("total_price").doubleValue());
        if (rs.getTimestamp("closed_at") != null) {
            order.setClosedAt(rs.getTimestamp("closed_at").toLocalDateTime());
        }

        loadOrderItems(order);
        return order;
    }

    private void loadOrderItems(Order order) {
        String sql = "SELECT book_id, quantity FROM order_items WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                order.getItems().put(rs.getInt("book_id"), rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке items заказа", e);
        }
    }

    @Override
    public Order save(Order order) {
        return TransactionalExecutor.executeInTransaction(conn -> {
            String sql = "INSERT INTO orders (customer_name, status, total_price, created_at, closed_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, order.getCustomerName());
                stmt.setString(2, order.getStatus().name());
                stmt.setBigDecimal(3, java.math.BigDecimal.valueOf(order.getTotalPrice()));
                stmt.setTimestamp(4, Timestamp.valueOf(order.getCreatedAt()));
                stmt.setTimestamp(5, order.getClosedAt() != null ? Timestamp.valueOf(order.getClosedAt()) : null);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    order.setId(rs.getInt("id"));
                }

                // Сохраняем связи в order_items
                saveOrderItems(conn, order.getId(), order.getItems());

                return order;
            }
        });
    }

    private void saveOrderItems(Connection conn, int orderId, Map<Integer, Integer> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, book_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, entry.getKey());
                stmt.setInt(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public Order update(Order order) {
        return TransactionalExecutor.executeInTransaction(conn -> {
            String sql = "UPDATE orders SET customer_name = ?, status = ?, total_price = ?, closed_at = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, order.getCustomerName());
                stmt.setString(2, order.getStatus().name());
                stmt.setBigDecimal(3, java.math.BigDecimal.valueOf(order.getTotalPrice()));
                stmt.setTimestamp(4, order.getClosedAt() != null ? Timestamp.valueOf(order.getClosedAt()) : null);
                stmt.setInt(5, order.getId());
                stmt.executeUpdate();
            }

            // Опционально: обновить order_items, если нужно
            return order;
        });
    }
}