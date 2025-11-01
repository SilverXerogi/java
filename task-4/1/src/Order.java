import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Order {
    public enum Status { NEW, COMPLETED, CANCELED }

    private final String id;
    private final Map<String, Integer> items; // bookId -> quantity
    private Status status;
    private LocalDateTime createdAt;   // дата создания
    private LocalDateTime completedAt; // дата завершения
    private double totalPrice;         // сумма заказа
    private String customerName;       // данные заказчика

    public Order(Map<String, Integer> items, String customerName) {
        this.id = UUID.randomUUID().toString();
        this.items = new HashMap<>(items);
        this.status = Status.NEW;
        this.createdAt = LocalDateTime.now();
        this.customerName = customerName;
    }

    // --- Геттеры и сеттеры ---
    public String getId() { return id; }
    public Map<String, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public double getTotalPrice() { return totalPrice; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    // --- Расчёт общей суммы ---
    public void calculateTotal(Map<String, Book> books) {
        totalPrice = 0;
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            Book book = books.get(entry.getKey());
            if (book != null) {
                totalPrice += book.getPrice() * entry.getValue();
            }
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customer='" + customerName + '\'' +
                ", items=" + items +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }
}
