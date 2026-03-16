package bookstore.model.factory;



import bookstore.model.Book;

import java.time.LocalDate;

public class BookFactory {

    public Book createBook(int id, String title, Book.Status status) {
        return new Book(id, title, status);
    }

    public Book createBook(int id, String title, Book.Status status, double price, LocalDate publicationDate, String description) {
        return new Book(id, title, status, price, publicationDate, description);
    }
}
