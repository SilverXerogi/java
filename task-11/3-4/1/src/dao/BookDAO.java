package dao;

import jdbc.ConnectionManager;
import model.Book;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookDAO implements GenericDAO<Book, Integer> {

    @Override
    public Book findById(Integer id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToBook(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске книги", e);
        }
        return null;
    }

    @Override
    public List<Book> findAll() {
        String sql = "SELECT * FROM books";
        List<Book> books = new ArrayList<>();
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех книг", e);
        }
        return books;
    }

    @Override
    public Book save(Book book) {
        String sql = "INSERT INTO books (title, status, price, publication_date, arrival_date, description) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getStatus().name());
            stmt.setBigDecimal(3, java.math.BigDecimal.valueOf(book.getPrice()));
            stmt.setDate(4, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);
            stmt.setDate(5, book.getArrivalDate() != null ? Date.valueOf(book.getArrivalDate()) : null);
            stmt.setString(6, book.getDescription());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                book.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении книги", e);
        }
        return book;
    }

    @Override
    public Book update(Book book) {
        String sql = "UPDATE books SET title = ?, status = ?, price = ?, publication_date = ?, arrival_date = ?, description = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getStatus().name());
            stmt.setBigDecimal(3, java.math.BigDecimal.valueOf(book.getPrice()));
            stmt.setDate(4, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);
            stmt.setDate(5, book.getArrivalDate() != null ? Date.valueOf(book.getArrivalDate()) : null);
            stmt.setString(6, book.getDescription());
            stmt.setInt(7, book.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении книги", e);
        }
        return book;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = ConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении книги", e);
        }
    }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        Book book = new Book(
                rs.getInt("id"),
                rs.getString("title"),
                model.Book.Status.valueOf(rs.getString("status")),
                rs.getBigDecimal("price").doubleValue(),
                rs.getDate("publication_date") != null ? rs.getDate("publication_date").toLocalDate() : null,
                rs.getString("description")
        );
        book.setArrivalDate(rs.getDate("arrival_date") != null ? rs.getDate("arrival_date").toLocalDate() : LocalDate.now());
        return book;
    }
}