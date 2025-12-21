package model;

import model.*;
import model.factory.DefaultBookStoreFactory;
import model.persistence.AppState;
import model.persistence.StateSerializer;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BookStoreServiceImpl implements BookStoreService {

    private final Inventory inventory;
    private final DefaultBookStoreFactory factory;

    private final Map<Integer, Order> orders = new HashMap<>();
    private final Map<Integer, BookRequest> requests = new HashMap<>();
    private final Map<Integer, List<Integer>> requestsByBook = new HashMap<>();
    private final Map<Integer, LocalDate> orderDates = new HashMap<>();

    public BookStoreServiceImpl(Inventory inventory, DefaultBookStoreFactory factory) {
        this.inventory = inventory;
        this.factory = factory;
    }

    // Новый метод для восстановления состояния
    public void restoreFromState(AppState state) {
        this.orders.clear();
        this.requests.clear();
        this.requestsByBook.clear();
        this.orderDates.clear();

        this.orders.putAll(state.getOrders());
        this.requests.putAll(state.getRequests());
        this.requestsByBook.putAll(state.getRequestsByBook());
        this.orderDates.putAll(state.getOrderDates());
    }

    // --- Методы сервиса (как раньше) ---

    @Override
    public Order createOrder(Map<Integer, Integer> items, String customerName) {
        Order order = factory.createNewOrder(items, customerName);
        orders.put(order.getId(), order);
        orderDates.put(order.getId(), LocalDate.now());

        items.keySet().forEach(bookId -> {
            Book book = inventory.getBook(bookId);
            if (book == null || book.getStatus() == Book.Status.ABSENT) {
                createRequestIfAbsent(bookId);
            }
        });

        order.recalculateTotal(inventory); // <-- передаём inventory
        return order;
    }

    @Override
    public Map<Integer, Order> getAllOrders() {
        return orders;
    }

    private BookRequest createRequestIfAbsent(int bookId) {
        List<Integer> list = requestsByBook.getOrDefault(bookId, new ArrayList<>());

        for (int reqId : list) {
            BookRequest r = requests.get(reqId);
            if (r != null && r.getStatus() == BookRequest.Status.OPEN) {
                r.incrementCount();
                return r;
            }
        }

        BookRequest r = factory.createNewRequest(bookId);
        requests.put(r.getId(), r);
        requestsByBook.computeIfAbsent(bookId, k -> new ArrayList<>()).add(r.getId());
        return r;
    }

    @Override
    public void cancelOrder(int orderId) {
        Order order = orders.get(orderId);
        if (order != null) order.setStatus(Order.Status.CANCELLED);
    }

    @Override
    public void changeOrderStatus(int orderId, Order.Status status) {
        Order o = orders.get(orderId);
        if (o == null) return;

        if (status == Order.Status.COMPLETED) {
            boolean allAvailable = o.getItems().entrySet().stream().allMatch(e ->
                    inventory.getBook(e.getKey()) != null &&
                            inventory.getBook(e.getKey()).getStatus() == Book.Status.AVAILABLE &&
                            inventory.getQuantity(e.getKey()) >= e.getValue()
            );
            if (!allAvailable) return;

            o.getItems().forEach((id, qty) -> inventory.reduceStock(id, qty));
        }
        o.setStatus(status);
    }

    @Override
    public void addBookToInventory(int bookId, int qty) {
        Book book = inventory.getBook(bookId);
        if (book == null) return;
        inventory.addStock(bookId, qty);

        if (inventory.isAutoCloseRequestsOnStockAdd()) {
            requestsByBook.getOrDefault(bookId, List.of()).stream()
                    .map(requests::get)
                    .filter(Objects::nonNull)
                    .filter(r -> r.getStatus() == BookRequest.Status.OPEN)
                    .forEach(BookRequest::close);
        }
    }

    @Override
    public void writeOffBook(int bookId) {
        Book book = inventory.getBook(bookId);
        if (book != null) inventory.writeOff(bookId);
    }

    @Override
    public BookRequest requestBook(int bookId) {
        return createRequestIfAbsent(bookId);
    }

    @Override
    public Book getBookDetails(int bookId) {
        return inventory.getBook(bookId);
    }

    @Override
    public Order getOrderDetails(int orderId) {
        return orders.get(orderId);
    }

    @Override
    public List<Book> getBooksSortedByTitle() {
        return inventory.listBooksByTitle();
    }

    @Override
    public List<Book> getBooksSortedByPrice() {
        return inventory.listBooksByPrice();
    }

    @Override
    public List<Book> getBooksSortedByDate() {
        return inventory.listBooksByArrivalDate();
    }

    @Override
    public List<Book> getBooksSortedByAvailability() {
        return inventory.listBooksByAvailability();
    }

    @Override
    public List<Book> getStaleBooks(int months) {
        return inventory.listStaleBooks(months);
    }

    @Override
    public List<Order> getOrdersSortedByDate() {
        return orders.values().stream()
                .sorted(Comparator.comparing(o -> orderDates.get(o.getId())))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> getOrdersSortedByPrice() {
        return orders.values().stream()
                .sorted(Comparator.comparingDouble(Order::getTotalPrice))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersSortedByStatus() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::getStatus))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getCompletedOrdersInPeriod(LocalDate from, LocalDate to) {
        return orders.values().stream()
                .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                .filter(o -> {
                    LocalDate date = orderDates.get(o.getId());
                    return (date.isAfter(from) || date.isEqual(from)) &&
                            (date.isBefore(to) || date.isEqual(to));
                })
                .collect(Collectors.toList());
    }

    @Override
    public double getRevenueInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).stream()
                .peek(o -> o.recalculateTotal(inventory)) // <-- передаём inventory
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    @Override
    public int getCompletedOrdersCountInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).size();
    }

    @Override
    public List<BookRequest> getRequestsSortedByTitle() {
        return requests.values().stream()
                .sorted(Comparator.comparing(r -> inventory.getBook(r.getBookId()).getTitle()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookRequest> getRequestsSortedByRequestCount() {
        return requests.values().stream()
                .sorted(Comparator.comparingInt(BookRequest::getRequestCount).reversed())
                .collect(Collectors.toList());
    }

    public Map<Integer, BookRequest> getAllRequests() {
        return new HashMap<>(requests);
    }
}