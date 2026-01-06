package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { AVAILABLE, ABSENT }

    private int id; // <-- теперь mutable
    private String title;
    private Status status;
    private double price;
    private LocalDate publicationDate;
    private LocalDate arrivalDate;
    private String description;

    public Book(int id, String title, Status status) {
        this(id, title, status, 0.0, LocalDate.now(), "");
    }

    public Book(int id, String title, Status status, double price, LocalDate publicationDate, String description) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.price = price;
        this.publicationDate = publicationDate;
        this.arrivalDate = LocalDate.now();
        this.description = description;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } // <-- добавлен сеттер
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Book{" +
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