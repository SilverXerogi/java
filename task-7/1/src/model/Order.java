package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class Order implements Serializable { // Добавлен интерфейс
    private static final long serialVersionUID = 1L; // Уникальный ID для сериализации

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
        this.totalPrice = calculateTotal(items);
    }

    // --- Расчёт суммы ---
    private double calculateTotal(Map<Integer, Integer> items) {
        double sum = 0.0;
        Inventory inv = Inventory.getInstance();
        for (Map.Entry<Integer, Integer> e : items.entrySet()) {
            int bookId = e.getKey();
            int qty = e.getValue();
            double price = inv.getPrice(bookId);
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

    public void recalculateTotal() {
        this.totalPrice = calculateTotal(this.items);
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