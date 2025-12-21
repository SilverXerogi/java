package model.factory;

import model.Book;
import model.BookRequest;
import model.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class DefaultBookStoreFactory {

    private final BookFactory bookFactory = new BookFactory();
    private final OrderFactory orderFactory = new OrderFactory();
    private final BookRequestFactory requestFactory = new BookRequestFactory();

    private int bookCounter = 0;
    private int orderCounter = 0;
    private int requestCounter = 0;

    public DefaultBookStoreFactory() {}

    public int getBookCounter() { return bookCounter; }
    public int getOrderCounter() { return orderCounter; }
    public int getRequestCounter() { return requestCounter; }

    public BookFactory getBookFactory() { return bookFactory; }
    public OrderFactory getOrderFactory() { return orderFactory; }
    public BookRequestFactory getRequestFactory() { return requestFactory; }

    public Book createBook(String title, Book.Status status, double price, LocalDate publicationDate, String description) {
        bookCounter++;
        return bookFactory.createBook(bookCounter, title, status, price, publicationDate, description);
    }

    public Book createBook(String title, Book.Status status) {
        bookCounter++;
        return bookFactory.createBook(bookCounter, title, status);
    }

    public Order createOrder(Map<Integer, Integer> items, String customerName, LocalDateTime createdAt) {
        orderCounter++;
        return orderFactory.createOrder(orderCounter, items, customerName, createdAt);
    }

    public Order createNewOrder(Map<Integer, Integer> items, String customerName) {
        orderCounter++;
        return orderFactory.createNewOrder(orderCounter, items, customerName);
    }

    public BookRequest createRequest(int bookId, BookRequest.Status status, int requestCount, LocalDateTime createdAt) {
        requestCounter++;
        return requestFactory.createRequest(requestCounter, bookId, status, requestCount, createdAt);
    }

    public BookRequest createNewRequest(int bookId) {
        requestCounter++;
        return requestFactory.createNewRequest(requestCounter, bookId);
    }

    public void restoreCounters(int bookCounter, int orderCounter, int requestCounter) {
        this.bookCounter = bookCounter;
        this.orderCounter = orderCounter;
        this.requestCounter = requestCounter;
        System.out.println("[model.factory.DefaultBookStoreFactory] Счётчики восстановлены: book=" + bookCounter + ", order=" + orderCounter + ", request=" + requestCounter);
    }
}