package bookstore.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        CREATED, PAID, SHIPPED, COMPLETED, CANCELLED
    }

    private int id; // <-- mutable
    private final Map<Integer, Integer> items; // bookId -> quantity
    private final String customerName;
    private final LocalDateTime createdAt;
    private Status status;
    private double totalPrice;
    private LocalDateTime closedAt; // <-- добавлено

    public Order(int id, Map<Integer, Integer> items, String customerName, LocalDateTime createdAt) {
        this.id = id;
        this.items = new HashMap<>(items);
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.status = Status.CREATED;
        this.totalPrice = 0.0;
    }

    public void recalculateTotal(Inventory inventory) {
        double sum = 0.0;
        for (Map.Entry<Integer, Integer> e : items.entrySet()) {
            int bookId = e.getKey();
            int qty = e.getValue();
            double price = inventory.getPrice(bookId);
            sum += price * qty;
        }
        this.totalPrice = sum;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id; }
    public void setId(int id) {
        this.id = id;
    } // <-- добавлен сеттер
    public Map<Integer, Integer> getItems() {
        return new HashMap<>(items);
    }
    public String getCustomerName() {
        return customerName;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public LocalDateTime getClosedAt() {
        return closedAt;
    }
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    } // <-- добавлено

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer='" + customerName + '\'' +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                ", closedAt=" + closedAt +
                ", items=" + items +
                '}';
    }
}