import java.time.LocalDateTime;
import java.util.UUID;

public class BookRequest {
    public enum Status { OPEN, CLOSED }

    private final String id;
    private final String bookId;
    private Status status;
    private int requestCount;        // сколько раз эту книгу запрашивали
    private LocalDateTime createdAt; // когда запрос был создан
    private LocalDateTime closedAt;  // когда запрос был закрыт

    public BookRequest(String bookId) {
        this.id = UUID.randomUUID().toString();
        this.bookId = bookId;
        this.status = Status.OPEN;
        this.requestCount = 1;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public Status getStatus() { return status; }
    public int getRequestCount() { return requestCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getClosedAt() { return closedAt; }

    /** Увеличить количество запросов на ту же книгу */
    public void incrementCount() {
        this.requestCount++;
    }

    /** Закрыть запрос, когда книга поступила */
    public void close() {
        this.status = Status.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "BookRequest{" +
                "id='" + id + '\'' +
                ", bookId='" + bookId + '\'' +
                ", status=" + status +
                ", requestCount=" + requestCount +
                ", createdAt=" + createdAt +
                ", closedAt=" + closedAt +
                '}';
    }
}
