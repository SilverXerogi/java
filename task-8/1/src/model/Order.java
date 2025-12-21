package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        CREATED,    // создан, ожидает обработки
        PAID,       // оплачен
        SHIPPED,    // отправлен
        COMPLETED,  // выполнен
        CANCELLED   // отменён
    }

    private final int id;
    private final Map<Integer, Integer> items; // bookId -> quantity
    private final String customerName;
    private final LocalDateTime createdAt;
    private Status status;
    private double totalPrice;

    public Order(int id, Map<Integer, Integer> items, String customerName, LocalDateTime createdAt) {
        this.id = id;
        this.items = new HashMap<>(items);
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.status = Status.CREATED;
        // totalPrice инициализируется в конструкторе как 0.0
        // реальный расчёт будет происходить при вызове recalculateTotal
        this.totalPrice = 0.0;
    }

    // --- Расчёт суммы (теперь принимает Inventory как параметр) ---
    private double calculateTotal(Map<Integer, Integer> items, Inventory inventory) {
        double sum = 0.0;
        for (Map.Entry<Integer, Integer> e : items.entrySet()) {
            int bookId = e.getKey();
            int qty = e.getValue();
            double price = inventory.getPrice(bookId);
            sum += price * qty;
        }
        return sum;
    }

    public int getId() {
        return id;
    }

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    // --- Метод для пересчёта, теперь принимает Inventory ---
    public void recalculateTotal(Inventory inventory) {
        this.totalPrice = calculateTotal(this.items, inventory);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer='" + customerName + '\'' +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                '}';
    }
}