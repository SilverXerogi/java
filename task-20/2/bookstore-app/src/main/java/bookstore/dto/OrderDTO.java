package bookstore.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class OrderDTO {
    private int id;
    private String customerName;
    private String status; // PENDING, COMPLETED, CANCELLED
    private double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private Map<Integer, Integer> items; // bookId -> quantity

    // constructors
    public OrderDTO() { }

    public OrderDTO(int id, String customerName, String status, double totalPrice, LocalDateTime createdAt, LocalDateTime closedAt, Map<Integer, Integer> items) {
        this.id = id;
        this.customerName = customerName;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
        this.items = items;
    }

    // getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
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

    public Map<Integer, Integer> getItems() {
        return items;
    }
    public void setItems(Map<Integer, Integer> items) {
        this.items = items;
    }
}