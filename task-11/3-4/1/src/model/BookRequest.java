package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { OPEN, CLOSED }

    private int id; // <-- mutable
    private int bookId;
    private Status status;
    private int requestCount;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public BookRequest(int id, int bookId, Status status, int requestCount, LocalDateTime createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.status = status;
        this.requestCount = requestCount;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // <-- добавлен сеттер
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getRequestCount() { return requestCount; }
    public void setRequestCount(int requestCount) { this.requestCount = requestCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    // Методы поведения
    public void incrementCount() {
        this.requestCount++;
    }

    public void close() {
        this.status = Status.CLOSED;
        this.closedAt = LocalDateTime.now();
    }



    @Override
    public String toString() {
        return "BookRequest{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", status=" + status +
                ", requestCount=" + requestCount +
                ", createdAt=" + createdAt +
                ", closedAt=" + closedAt +
                '}';
    }
}