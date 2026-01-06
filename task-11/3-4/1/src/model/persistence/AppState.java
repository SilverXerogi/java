package model.persistence;

import model.Book;
import model.BookRequest;
import model.Inventory;
import model.Order;
import model.factory.DefaultBookStoreFactory;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

// Класс для хранения полного состояния приложения
public class AppState implements Serializable {
    private static final long serialVersionUID = 2L; // Увеличили ID, т.к. структура изменилась

    // --- Состояние инвентаря ---
    private final Map<Integer, Book> catalog;
    private final Map<Integer, Integer> stock;
    private final Map<Integer, LocalDate> arrivalDates;
    private final Map<Integer, Double> prices;
    private final Map<Integer, LocalDate> lastSoldDate;
    private final int staleMonthsThreshold;
    private final boolean autoCloseRequestsOnStockAdd;

    // --- Состояние сервиса ---
    private final Map<Integer, Order> orders;
    private final Map<Integer, BookRequest> requests;
    private final Map<Integer, List<Integer>> requestsByBook;
    private final Map<Integer, LocalDate> orderDates;

    // --- Состояние фабрики (счётчики) ---
    private final int bookCounter;
    private final int orderCounter;
    private final int requestCounter;

    // Конструктор для сохранения
    public AppState(Inventory inventory, Map<Integer, Order> orders, Map<Integer, BookRequest> requests, DefaultBookStoreFactory factory) {
        // Сохраняем состояние инвентаря
        this.catalog = new HashMap<>(inventory.getCatalogMap());
        this.stock = new HashMap<>(inventory.getStockMap());
        this.arrivalDates = new HashMap<>(inventory.getArrivalDatesMap());
        this.prices = new HashMap<>(inventory.getPriceMap());
        this.lastSoldDate = new HashMap<>(inventory.getLastSoldDateMap());
        this.staleMonthsThreshold = inventory.getStaleMonthsThreshold();
        this.autoCloseRequestsOnStockAdd = inventory.isAutoCloseRequestsOnStockAdd();

        // Сохраняем состояние сервиса
        this.orders = new HashMap<>(orders);
        this.requests = new HashMap<>(requests);
        // Предполагаем, что BookStoreServiceImpl предоставляет доступ к этим картам
        // Используем прямой доступ к полям BookStoreServiceImpl или добавим геттеры
        this.requestsByBook = new HashMap<>(); // Пока пусто, если нет геттера
        this.orderDates = new HashMap<>(); // Пока пусто, если нет геттера

        // Сохраняем счётчики фабрики
        // Это критически важно, чтобы новые ID не пересекались при восстановлении
        // Предположим, у DefaultBookStoreFactory есть публичные методы получения счётчиков
        this.bookCounter = factory.getBookCounter(); // Предполагаем, что такой метод есть
        this.orderCounter = factory.getOrderCounter(); // Предполагаем, что такой метод есть
        this.requestCounter = factory.getRequestCounter(); // Предполагаем, что такой метод есть
    }

    // Конструктор для загрузки (внутренний)
    public AppState(
            Map<Integer, Book> catalog,
            Map<Integer, Integer> stock,
            Map<Integer, LocalDate> arrivalDates,
            Map<Integer, Double> prices,
            Map<Integer, LocalDate> lastSoldDate,
            int staleMonthsThreshold,
            boolean autoCloseRequestsOnStockAdd,
            Map<Integer, Order> orders,
            Map<Integer, BookRequest> requests,
            Map<Integer, List<Integer>> requestsByBook,
            Map<Integer, LocalDate> orderDates,
            int bookCounter,
            int orderCounter,
            int requestCounter
    ) {
        this.catalog = catalog;
        this.stock = stock;
        this.arrivalDates = arrivalDates;
        this.prices = prices;
        this.lastSoldDate = lastSoldDate;
        this.staleMonthsThreshold = staleMonthsThreshold;
        this.autoCloseRequestsOnStockAdd = autoCloseRequestsOnStockAdd;
        this.orders = orders;
        this.requests = requests;
        this.requestsByBook = requestsByBook;
        this.orderDates = orderDates;
        this.bookCounter = bookCounter;
        this.orderCounter = orderCounter;
        this.requestCounter = requestCounter;
    }

    // Геттеры
    public Map<Integer, Book> getCatalog() { return catalog; }
    public Map<Integer, Integer> getStock() { return stock; }
    public Map<Integer, LocalDate> getArrivalDates() { return arrivalDates; }
    public Map<Integer, Double> getPrices() { return prices; }
    public Map<Integer, LocalDate> getLastSoldDate() { return lastSoldDate; }
    public int getStaleMonthsThreshold() { return staleMonthsThreshold; }
    public boolean isAutoCloseRequestsOnStockAdd() { return autoCloseRequestsOnStockAdd; }
    public Map<Integer, Order> getOrders() { return orders; }
    public Map<Integer, BookRequest> getRequests() { return requests; }
    public Map<Integer, List<Integer>> getRequestsByBook() { return requestsByBook; }
    public Map<Integer, LocalDate> getOrderDates() { return orderDates; }
    // Геттеры для счётчиков
    public int getBookCounter() { return bookCounter; }
    public int getOrderCounter() { return orderCounter; }
    public int getRequestCounter() { return requestCounter; }
}