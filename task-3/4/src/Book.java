
public class Book {
    public enum Status { AVAILABLE, ABSENT }

    private final String id;
    private final String title;
    private Status status;

    public Book(String id, String title, Status status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "Book{" + "id='" + id + '\'' + ", title='" + title + '\'' + ", status=" + status + '}';
    }
}
