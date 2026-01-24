package model;

import dao.BookDAO;
import dao.OrderDAO;
import dao.BookRequestDAO;
import jdbc.TransactionalExecutor;
import model.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BookStoreServiceImpl implements BookStoreService {

    private final Inventory inventory;
    private final model.factory.DefaultBookStoreFactory factory;

    // DAO-объекты
    private final BookDAO bookDAO = new BookDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final BookRequestDAO requestDAO = new BookRequestDAO();

    public BookStoreServiceImpl(Inventory inventory, model.factory.DefaultBookStoreFactory factory) {
        this.inventory = inventory;
        this.factory = factory;
    }

    // --- BookService ---

    @Override
    public void addBookToInventory(int bookId, int qty) {
        TransactionalExecutor.executeInTransaction(conn -> {
            Book book = bookDAO.findById(bookId);
            if (book == null) return null;

            inventory.addStock(bookId, qty);

            if (inventory.isAutoCloseRequestsOnStockAdd()) {
                List<BookRequest> openRequests = requestDAO.findAll().stream()
                        .filter(r -> r.getBookId() == bookId && r.getStatus() == BookRequest.Status.OPEN)
                        .toList();
                for (BookRequest r : openRequests) {
                    r.close();
                    requestDAO.update(r);
                }
            }

            return null;
        });
    }

    @Override
    public void writeOffBook(int bookId) {
        Book book = bookDAO.findById(bookId);
        if (book != null) inventory.writeOff(bookId);
    }

    @Override
    public BookRequest requestBook(int bookId) {
        BookRequest request = new BookRequest(0, bookId, BookRequest.Status.OPEN, 1, java.time.LocalDateTime.now());
        return requestDAO.save(request);
    }

    @Override
    public Book getBookDetails(int bookId) {
        return bookDAO.findById(bookId);
    }

    @Override
    public List<Book> getBooksSortedByTitle() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparing(Book::getTitle))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksSortedByPrice() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparingDouble(Book::getPrice))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksSortedByDate() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparing(Book::getArrivalDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksSortedByAvailability() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparing(Book::getStatus))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getStaleBooks(int months) {
        return inventory.listStaleBooks(months); // вызов из инвентаря
    }

    // --- OrderService ---

    @Override
    public Order createOrder(Map<Integer, Integer> items, String customerName) {
        return TransactionalExecutor.executeInTransaction(conn -> {
            Order order = factory.createNewOrder(items, customerName);
            order.recalculateTotal(inventory);
            Order saved = orderDAO.save(order);

            // Проверяем отсутствие книг — создаём заявки
            items.keySet().forEach(bookId -> {
                Book book = inventory.getBook(bookId);
                if (book == null || book.getStatus() == Book.Status.ABSENT) {
                    createRequestIfAbsent(bookId);
                }
            });

            return saved;
        });
    }

    @Override
    public Map<Integer, Order> getAllOrders() {
        List<Order> orders = orderDAO.findAll();
        Map<Integer, Order> map = new HashMap<>();
        for (Order o : orders) {
            map.put(o.getId(), o);
        }
        return map;
    }

    @Override
    public void cancelOrder(int orderId) {
        TransactionalExecutor.executeInTransaction(conn -> {
            Order order = orderDAO.findById(orderId);
            if (order != null) {
                order.setStatus(Order.Status.CANCELLED);
                orderDAO.update(order);
            }
            return null;
        });
    }

    @Override
    public void changeOrderStatus(int orderId, Order.Status status) {
        TransactionalExecutor.executeInTransaction(conn -> {
            Order o = orderDAO.findById(orderId);
            if (o == null) return null;

            if (status == Order.Status.COMPLETED) {
                boolean allAvailable = o.getItems().entrySet().stream().allMatch(e ->
                        inventory.getBook(e.getKey()) != null &&
                                inventory.getBook(e.getKey()).getStatus() == Book.Status.AVAILABLE &&
                                inventory.getQuantity(e.getKey()) >= e.getValue()
                );
                if (!allAvailable) throw new RuntimeException("Не все книги доступны для завершения заказа");

                o.getItems().forEach((id, qty) -> inventory.reduceStock(id, qty));
            }
            o.setStatus(status);
            orderDAO.update(o);
            return null;
        });
    }

    @Override
    public Order getOrderDetails(int orderId) {
        return orderDAO.findById(orderId);
    }

    @Override
    public List<Order> getOrdersSortedByDate() {
        return orderDAO.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersSortedByPrice() {
        return orderDAO.findAll().stream()
                .sorted(Comparator.comparingDouble(Order::getTotalPrice))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersSortedByStatus() {
        return orderDAO.findAll().stream()
                .sorted(Comparator.comparing(Order::getStatus))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getCompletedOrdersInPeriod(LocalDate from, LocalDate to) {
        return orderDAO.findAll().stream()
                .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                .filter(o -> {
                    LocalDate date = o.getCreatedAt().toLocalDate();
                    return (date.isAfter(from) || date.isEqual(from)) &&
                            (date.isBefore(to) || date.isEqual(to));
                })
                .collect(Collectors.toList());
    }

    @Override
    public double getRevenueInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).stream()
                .peek(o -> o.recalculateTotal(inventory))
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    @Override
    public int getCompletedOrdersCountInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).size();
    }

    // --- RequestService ---

    @Override
    public List<BookRequest> getRequestsSortedByTitle() {
        return requestDAO.findAll().stream()
                .sorted(Comparator.comparing(r -> {
                    Book book = bookDAO.findById(r.getBookId());
                    return book != null ? book.getTitle() : "";
                }))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookRequest> getRequestsSortedByRequestCount() {
        return requestDAO.findAll().stream()
                .sorted(Comparator.comparingInt(BookRequest::getRequestCount).reversed())
                .collect(Collectors.toList());
    }

    public Map<Integer, BookRequest> getAllRequests() {
        List<BookRequest> requests = requestDAO.findAll();
        Map<Integer, BookRequest> map = new HashMap<>();
        for (BookRequest r : requests) {
            map.put(r.getId(), r);
        }
        return map;
    }

    private BookRequest createRequestIfAbsent(int bookId) {
        BookRequest request = new BookRequest(0, bookId, BookRequest.Status.OPEN, 1, java.time.LocalDateTime.now());
        return requestDAO.save(request);
    }
}