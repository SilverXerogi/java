package model;

import java.time.LocalDateTime;

public class BookRequest {
    public enum Status { OPEN, CLOSED }

    private final int id;
    private final int bookId;
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

    // --- Геттеры ---
    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public Status getStatus() { return status; }
    public int getRequestCount() { return requestCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getClosedAt() { return closedAt; }

    // --- Методы поведения ---
    public void incrementCount() {
        this.requestCount++;
    }

    public void close() {
        this.status = Status.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
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
