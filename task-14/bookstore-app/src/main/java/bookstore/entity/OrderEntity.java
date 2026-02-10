package bookstore.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String customerName;
    @Enumerated(EnumType.STRING)
    private Status status;
    private double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "book_id")
    @Column(name = "quantity")
    private Map<Integer, Integer> items = new HashMap<>();

    public enum Status {
        PENDING, COMPLETED, CANCELLED
    }

    // Constructors
    public OrderEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public OrderEntity(String customerName, Map<Integer, Integer> items) {
        this();
        this.customerName = customerName;
        this.items = items;
        this.status = Status.PENDING;
    }

    // Getters and Setters
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    @Override
    public String toString() {
        return "OrderEntity{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                ", closedAt=" + closedAt +
                ", items=" + items +
                '}';
    }
}