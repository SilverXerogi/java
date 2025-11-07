package service;

import model.Book;
import model.BookRequest;
import model.Inventory;
import model.Order;

import java.util.*;
import java.util.stream.Collectors;

public class BookStoreServiceImpl implements BookStoreService {
    private final Inventory inventory;
    private final Map<String, Order> orders = new HashMap<>();
    private final Map<String, BookRequest> requests = new HashMap<>();
    private final Map<String, List<String>> requestsByBook = new HashMap<>();
    private final Map<String, Date> orderDates = new HashMap<>(); // дата создания заказа

    public BookStoreServiceImpl(Inventory inventory) {
        this.inventory = inventory;
    }

    // ------------------- Заказы -------------------
    public Order createOrder(Map<String, Integer> items) {
        Order order = new Order(items, "Default Customer");
        orders.put(order.getId(), order);
        orderDates.put(order.getId(), new Date());

        // создаём запросы на отсутствующие книги
        for (String bookId : items.keySet()) {
            Book book = inventory.getBook(bookId);
            if (book == null || book.getStatus() == Book.Status.ABSENT) {
                createRequestIfAbsent(bookId);
            }
        }
        return order;
    }

    private BookRequest createRequestIfAbsent(String bookId) {
        List<String> list = requestsByBook.getOrDefault(bookId, new ArrayList<>());
        for (String reqId : list) {
            BookRequest r = requests.get(reqId);
            if (r != null && r.getStatus() == BookRequest.Status.OPEN) {
                r.incrementCount();
                return r;
            }
        }
        BookRequest r = new BookRequest(bookId);
        requests.put(r.getId(), r);
        requestsByBook.computeIfAbsent(bookId, k -> new ArrayList<>()).add(r.getId());
        return r;
    }

    public void cancelOrder(String orderId) {
        Order o = orders.get(orderId);
        if (o != null) o.setStatus(Order.Status.CANCELED);
    }

    public void changeOrderStatus(String orderId, Order.Status status) {
        Order o = orders.get(orderId);
        if (o == null) return;
        if (status == Order.Status.COMPLETED) {
            boolean allAvailable = true;
            for (Map.Entry<String,Integer> e : o.getItems().entrySet()) {
                Book b = inventory.getBook(e.getKey());
                if (b == null || b.getStatus() == Book.Status.ABSENT || inventory.getQuantity(e.getKey()) < e.getValue()) {
                    allAvailable = false;
                }
            }
            if (!allAvailable) return;
            for (Map.Entry<String,Integer> e : o.getItems().entrySet()) {
                inventory.reduceStock(e.getKey(), e.getValue());
            }
        }
        o.setStatus(status);
    }

    // ------------------- Книги -------------------
    public void addBookToInventory(String bookId, int qty) {
        Book book = inventory.getBook(bookId);
        if (book == null) return;
        inventory.addStock(bookId, qty);
        // закрыть открытые запросы
        List<String> list = requestsByBook.getOrDefault(bookId, Collections.emptyList());
        for (String reqId : new ArrayList<>(list)) {
            BookRequest r = requests.get(reqId);
            if (r != null && r.getStatus() == BookRequest.Status.OPEN) {
                r.close();
            }
        }
    }

    public BookRequest requestBook(String bookId) {
        return createRequestIfAbsent(bookId);
    }

    public void writeOffBook(String bookId) {
        Book book = inventory.getBook(bookId);
        if (book != null) inventory.writeOff(bookId);
    }

    // ------------------- Сортировка книг -------------------
    public List<Book> listBooksSortedByTitle() {
        return inventory.listBooksByTitle();
    }

    public List<Book> listBooksSortedByPrice() {
        return inventory.listBooksByPrice();
    }

    public List<Book> listBooksSortedByDate() {
        return inventory.listBooksByArrivalDate();
    }

    public List<Book> listBooksSortedByAvailability() {
        return inventory.listBooksByAvailability();
    }

    public List<Book> listStaleBooks(int months) {
        return inventory.listStaleBooks(months);
    }

    // ------------------- Сортировка заказов -------------------
    public List<Order> listOrdersSortedByDate() {
        return orders.values().stream()
                .sorted(Comparator.comparing(o -> orderDates.get(o.getId())))
                .collect(Collectors.toList());
    }

    public List<Order> listOrdersSortedByPrice() {
        return orders.values().stream()
                .sorted(Comparator.comparingDouble(Order::getTotalPrice))
                .collect(Collectors.toList());
    }

    public List<Order> listOrdersSortedByStatus() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::getStatus))
                .collect(Collectors.toList());
    }

    // ------------------- Сортировка запросов -------------------
    public List<BookRequest> listRequestsSortedByTitle() {
        return requests.values().stream()
                .sorted(Comparator.comparing(r -> inventory.getBook(r.getBookId()).getTitle()))
                .collect(Collectors.toList());
    }

    public List<BookRequest> listRequestsSortedByRequestCount() {
        return requests.values().stream()
                .sorted(Comparator.comparingInt(BookRequest::getRequestCount).reversed())
                .collect(Collectors.toList());
    }

    // ------------------- Отчёты по заказам -------------------
    public List<Order> listCompletedOrdersInPeriod(Date from, Date to) {
        return orders.values().stream()
                .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                .filter(o -> {
                    Date date = orderDates.get(o.getId());
                    return !date.before(from) && !date.after(to);
                })
                .collect(Collectors.toList());
    }

    public double getRevenueInPeriod(Date from, Date to) {
        // Для примера считаем сумму заказов по цене книг
        return listCompletedOrdersInPeriod(from, to).stream()
                .peek(o -> o.calculateTotal(inventory.getCatalogMap()))
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    public int getCompletedOrdersCountInPeriod(Date from, Date to) {
        return listCompletedOrdersInPeriod(from, to).size();
    }

    // ------------------- Детали -------------------
    public Order getOrderDetails(String orderId) {
        return orders.get(orderId);
    }

    public Book getBookDetails(String bookId) {
        return inventory.getBook(bookId);
    }
}
