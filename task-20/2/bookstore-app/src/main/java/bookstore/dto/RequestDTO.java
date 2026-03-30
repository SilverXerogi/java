package bookstore.dto;

import java.time.LocalDateTime;

public class RequestDTO {
    private int id;
    private int bookId;
    private String status; // OPEN, CLOSED
    private int requestCount;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    // constructors
    public RequestDTO() { }

    public RequestDTO(int id, int bookId, String status, int requestCount, LocalDateTime createdAt, LocalDateTime closedAt) {
        this.id = id;
        this.bookId = bookId;
        this.status = status;
        this.requestCount = requestCount;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    // getters and setters
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

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
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
}