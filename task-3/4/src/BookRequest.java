
import java.util.UUID;

public class BookRequest {
    public enum Status { OPEN, CLOSED }

    private final String id;
    private final String bookId;
    private Status status;

    public BookRequest(String bookId) {
        this.id = UUID.randomUUID().toString();
        this.bookId = bookId;
        this.status = Status.OPEN;
    }

    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public Status getStatus() { return status; }
    public void close() { this.status = Status.CLOSED; }

    @Override
    public String toString() {
        return "BookRequest{" + "id='" + id + '\'' + ", bookId='" + bookId + '\'' + ", status=" + status + '}';
    }
}
