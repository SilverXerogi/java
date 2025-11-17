package service;

import model.Book;

import java.util.Date;
import java.util.List;

public interface BookService {
    void addBookToInventory(String bookId, int qty);
    void writeOffBook(String bookId);


    // --- Просмотр данных и сортировка ---
    List<model.Book> listBooksSortedByTitle();
    List<model.Book> listBooksSortedByPrice();
    List<model.Book> listBooksSortedByDate();
    List<model.Book> listBooksSortedByAvailability();





    List<model.Book> listStaleBooks(int months); // книги не проданы более чем months месяцев

    // --- Детали ---
    Book getBookDetails(String bookId);
}
