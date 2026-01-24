package bookstore.dao;

import bookstore.jdbc.ConnectionManager;
import bookstore.model.BookRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BookRequestDAO implements GenericDAO<BookRequest, Integer> {

    @Override
    public BookRequest findById(Integer id) {
        String sql = "SELECT * FROM book_requests WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToBookRequest(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске заявки", e);
        }
        return null;
    }

    @Override
    public List<BookRequest> findAll() {
        String sql = "SELECT * FROM book_requests";
        List<BookRequest> requests = new ArrayList<>();
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(mapRowToBookRequest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех заявок", e);
        }
        return requests;
    }

    @Override
    public BookRequest save(BookRequest request) {
        String sql = "INSERT INTO book_requests (book_id, status, request_count, created_at, closed_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getBookId());
            stmt.setString(2, request.getStatus().name());
            stmt.setInt(3, request.getRequestCount());
            stmt.setTimestamp(4, Timestamp.valueOf(request.getCreatedAt()));
            stmt.setTimestamp(5, request.getClosedAt() != null ? Timestamp.valueOf(request.getClosedAt()) : null);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                request.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении заявки", e);
        }
        return request;
    }

    @Override
    public BookRequest update(BookRequest request) {
        String sql = "UPDATE book_requests SET book_id = ?, status = ?, request_count = ?, closed_at = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getBookId());
            stmt.setString(2, request.getStatus().name());
            stmt.setInt(3, request.getRequestCount());
            stmt.setTimestamp(4, request.getClosedAt() != null ? Timestamp.valueOf(request.getClosedAt()) : null);
            stmt.setInt(5, request.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении заявки", e);
        }
        return request;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM book_requests WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении заявки", e);
        }
    }

    private BookRequest mapRowToBookRequest(ResultSet rs) throws SQLException {
        BookRequest request = new BookRequest(
                rs.getInt("id"),
                rs.getInt("book_id"),
                BookRequest.Status.valueOf(rs.getString("status")),
                rs.getInt("request_count"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
        if (rs.getTimestamp("closed_at") != null) {
            request.setClosedAt(rs.getTimestamp("closed_at").toLocalDateTime());
        }
        return request;
    }
}