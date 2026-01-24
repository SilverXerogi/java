package model;

import model.factory.DefaultBookStoreFactory;
import model.persistence.AppState;
import model.persistence.StateSerializer;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BookStoreServiceImpl implements BookStoreService {

    private static BookStoreServiceImpl instance;
    private final Inventory inventory;
    private final DefaultBookStoreFactory factory; // Теперь сохраняем ссылку на фабрику

    private final Map<Integer, Order> orders = new HashMap<>();
    private final Map<Integer, BookRequest> requests = new HashMap<>();
    private final Map<Integer, List<Integer>> requestsByBook = new HashMap<>();
    private final Map<Integer, LocalDate> orderDates = new HashMap<>();

    private BookStoreServiceImpl(Inventory inventory, DefaultBookStoreFactory factory) { // Принимаем фабрику
        this.inventory = inventory;
        this.factory = factory; // Сохраняем
    }

    // Приватный конструктор для загрузки из состояния
    private BookStoreServiceImpl(Inventory inventory, DefaultBookStoreFactory factory, Map<Integer, Order> loadedOrders, Map<Integer, BookRequest> loadedRequests, Map<Integer, List<Integer>> loadedRequestsByBook, Map<Integer, LocalDate> loadedOrderDates) {
        this.inventory = inventory;
        this.factory = factory; // Сохраняем
        this.orders.putAll(loadedOrders);
        this.requests.putAll(loadedRequests);
        this.requestsByBook.putAll(loadedRequestsByBook);
        this.orderDates.putAll(loadedOrderDates);
    }

    // Метод для получения инстанса с загрузкой из состояния при необходимости
    public static synchronized BookStoreServiceImpl getInstance(Inventory inventory, boolean loadFromState) {
        if (instance == null) {
            DefaultBookStoreFactory factory = DefaultBookStoreFactory.getInstance(); // Получаем фабрику

            if (loadFromState) {
                AppState loadedState = StateSerializer.loadState();
                if (loadedState != null) {
                    // Инициализируем инвентарь из состояния
                    inventory.restoreFromState(loadedState);
                    // Восстанавливаем счётчики фабрики
                    factory.restoreCounters(loadedState.getBookCounter(), loadedState.getOrderCounter(), loadedState.getRequestCounter()); // Предполагаем, что такой метод есть
                    // Создаем сервис с загруженными данными
                    instance = new BookStoreServiceImpl(inventory, factory, loadedState.getOrders(), loadedState.getRequests(), loadedState.getRequestsByBook(), loadedState.getOrderDates());
                    System.out.println("Состояние BookStoreServiceImpl восстановлено из файла.");
                    return instance;
                }
            }
            // Если состояние не загружено, создаем новый инстанс
            instance = new BookStoreServiceImpl(inventory, factory); // Передаём фабрику
        }
        return instance;
    }

    public static synchronized BookStoreServiceImpl getInstance(Inventory inventory) {
        return getInstance(inventory, false);
    }

    // --- Новый метод для восстановления счётчиков фабрики (внутри BookStoreServiceImpl) ---
    private void restoreFactoryCounters(AppState state) {
        DefaultBookStoreFactory factory = DefaultBookStoreFactory.getInstance();
        factory.restoreCounters(state.getBookCounter(), state.getOrderCounter(), state.getRequestCounter());
    }

    // --- Новые методы для сериализации ---
    public Map<Integer, List<Integer>> getRequestsByBook() {
        return new HashMap<>(requestsByBook);
    }

    public Map<Integer, LocalDate> getOrderDates() {
        return new HashMap<>(orderDates);
    }

    // ===================== ОСНОВНЫЕ МЕТОДЫ =====================

    @Override
    public Order createOrder(Map<Integer, Integer> items, String customerName) {
        Order order = factory.createNewOrder(items, customerName);
        orders.put(order.getId(), order);
        orderDates.put(order.getId(), LocalDate.now());

        // Проверка на отсутствие книг — создаём заявки
        items.keySet().forEach(bookId -> {
            Book book = inventory.getBook(bookId);
            if (book == null || book.getStatus() == Book.Status.ABSENT) {
                createRequestIfAbsent(bookId);
            }
        });

        order.recalculateTotal();
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

        // --- Новая логика: автоматическое закрытие заявок ---
        if (inventory.isAutoCloseRequestsOnStockAdd()) {
            requestsByBook.getOrDefault(bookId, List.of()).stream()
                    .map(requests::get)
                    .filter(Objects::nonNull)
                    .filter(r -> r.getStatus() == BookRequest.Status.OPEN)
                    .forEach(BookRequest::close);
        }
        // --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
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

    // ===================== СОРТИРОВКИ КНИГ =====================

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
        // Теперь используем перегруженный метод, который может использовать конфиг
        // или переданный параметр. Для совместимости оставим передачу параметра.
        return inventory.listStaleBooks(months);
    }

    // ===================== СОРТИРОВКИ ЗАКАЗОВ =====================

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


    public List<Order> listOrdersSortedByPrice() {
        return getOrdersSortedByPrice();
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

    // ===================== СТАТИСТИКА =====================

    @Override
    public double getRevenueInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).stream()
                .peek(Order::recalculateTotal)
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    @Override
    public int getCompletedOrdersCountInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).size();
    }

    // ===================== СОРТИРОВКИ ЗАПРОСОВ =====================

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