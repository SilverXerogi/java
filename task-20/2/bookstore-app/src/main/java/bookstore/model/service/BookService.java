package bookstore.model.service;

import bookstore.model.Book;
import bookstore.model.BookRequest;

import java.util.List;

public interface BookService {
    void addBookToInventory(int bookId, int qty);
    void writeOffBook(int bookId);
    BookRequest requestBook(int bookId);
    Book getBookDetails(int bookId);

    List<Book> getBooksSortedByTitle();
    List<Book> getBooksSortedByPrice();
    List<Book> getBooksSortedByDate();
    List<Book> getBooksSortedByAvailability();
    List<Book> getStaleBooks(int months);
}
