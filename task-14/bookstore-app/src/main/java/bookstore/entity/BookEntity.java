package bookstore.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "books")
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    @Enumerated(EnumType.STRING)
    private Status status;
    private double price;
    private LocalDate publicationDate;
    private LocalDate arrivalDate;
    private String description;

    public enum Status {
        AVAILABLE, ABSENT
    }

    // Constructors
    public BookEntity() {}

    public BookEntity(int id, String title, Status status, double price, LocalDate publicationDate, String description) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.price = price;
        this.publicationDate = publicationDate;
        this.arrivalDate = LocalDate.now();
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BookEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", price=" + price +
                ", publicationDate=" + publicationDate +
                ", arrivalDate=" + arrivalDate +
                ", description='" + description + '\'' +
                '}';
    }
}