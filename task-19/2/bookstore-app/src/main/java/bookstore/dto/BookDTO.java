package bookstore.dto;

public class BookDTO {
    private int id;
    private String title;
    private String status; // AVAILABLE, ABSENT
    private double price;
    private String publicationDate;
    private String arrivalDate;
    private String description;

    // constructors
    public BookDTO() { }

    public BookDTO(int id, String title, String status, double price, String publicationDate, String arrivalDate, String description) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.price = price;
        this.publicationDate = publicationDate;
        this.arrivalDate = arrivalDate;
        this.description = description;
    }

    // getters and setters
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

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getPublicationDate() {
        return publicationDate;
    }
    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }
    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}