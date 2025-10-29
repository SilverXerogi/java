
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Order {
    public enum Status { NEW, COMPLETED, CANCELED }

    private final String id;
    private final Map<String, Integer> items; // bookId -> qty
    private Status status;

    public Order(Map<String, Integer> items) {
        this.id = UUID.randomUUID().toString();
        this.items = new HashMap<>(items);
        this.status = Status.NEW;
    }

    public String getId() { return id; }
    public Map<String, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "Order{" + "id='" + id + '\'' + ", items=" + items + ", status=" + status + '}';
    }
}
