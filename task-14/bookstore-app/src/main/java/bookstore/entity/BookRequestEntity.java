package bookstore.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_requests")
public class BookRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int bookId;
    @Enumerated(EnumType.STRING)
    private Status status;
    private int requestCount;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public enum Status {
        OPEN, CLOSED
    }

    // Constructors
    public BookRequestEntity() {
        this.createdAt = LocalDateTime.now();
        this.status = Status.OPEN;
    }

    public BookRequestEntity(int bookId, int requestCount) {
        this();
        this.bookId = bookId;
        this.requestCount = requestCount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void close() {
        this.status = Status.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "BookRequestEntity{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", status=" + status +
                ", requestCount=" + requestCount +
                ", createdAt=" + createdAt +
                ", closedAt=" + closedAt +
                '}';
    }
}