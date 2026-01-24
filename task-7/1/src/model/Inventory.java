package model;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Properties;

public class Inventory {
    private static Inventory instance; // Singleton

    private final Map<Integer, Book> catalog = new HashMap<>();
    private final Map<Integer, Integer> stock = new HashMap<>();
    private final Map<Integer, LocalDate> arrivalDates = new HashMap<>();
    private final Map<Integer, Double> prices = new HashMap<>();
    private final Map<Integer, LocalDate> lastSoldDate = new HashMap<>();

    // --- Новые поля для конфигурации ---
    private int staleMonthsThreshold = 6; // Значение по умолчанию
    private boolean autoCloseRequestsOnStockAdd = true; // Значение по умолчанию
    // --- --- --- --- --- --- --- --- --- --- --- --- ---

    private Inventory() {
        loadConfig(); // Загружаем конфиг при создании инстанса
    }

    public static synchronized Inventory getInstance() {
        if (instance == null) {
            instance = new Inventory();
        }
        return instance;
    }

    // --- Новый метод для загрузки конфигурации ---
    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("[model.Inventory] Файл config.properties не найден в classpath, используются значения по умолчанию.");
                return;
            }
            props.load(input);
            this.staleMonthsThreshold = Integer.parseInt(props.getProperty("stale_months", String.valueOf(this.staleMonthsThreshold)));
            this.autoCloseRequestsOnStockAdd = Boolean.parseBoolean(props.getProperty("auto_close_requests_on_stock_add", String.valueOf(this.autoCloseRequestsOnStockAdd)));
            System.out.println("[model.Inventory] Конфигурация загружена: stale_months=" + this.staleMonthsThreshold + ", auto_close_requests_on_stock_add=" + this.autoCloseRequestsOnStockAdd);
        } catch (IOException e) {
            System.out.println("[model.Inventory] Ошибка при загрузке config.properties, используются значения по умолчанию. Ошибка: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[model.Inventory] Ошибка при чтении числового значения из config.properties, используются значения по умолчанию. Ошибка: " + e.getMessage());
        }
    }
    // --- --- --- --- --- --- --- --- --- --- --- --- ---


    // Регистрация книги в каталоге
    public void registerBook(Book book, int initialQty, double price) {
        int id = book.getId();
        catalog.put(id, book);
        stock.put(id, Math.max(0, initialQty));
        prices.put(id, price);
        arrivalDates.put(id, LocalDate.now());

        if (initialQty > 0) {
            book.setStatus(Book.Status.AVAILABLE);
        } else {
            book.setStatus(Book.Status.ABSENT);
        }

        System.out.println("[model.Inventory] Зарегистрирована книга: " + book + ", qty=" + initialQty + ", price=" + price);
    }

    public Map<Integer, Book> getCatalogMap() {
        return new HashMap<>(catalog);
    }

    public Book getBook(int bookId) {
        return catalog.get(bookId);
    }

    public int getQuantity(int bookId) {
        return stock.getOrDefault(bookId, 0);
    }

    public double getPrice(int bookId) {
        return prices.getOrDefault(bookId, 0.0);
    }

    public LocalDate getArrivalDate(int bookId) {
        return arrivalDates.get(bookId);
    }

    // --- Геттеры для конфигурации ---
    public int getStaleMonthsThreshold() {
        return staleMonthsThreshold;
    }

    public boolean isAutoCloseRequestsOnStockAdd() {
        return autoCloseRequestsOnStockAdd;
    }
    // --- --- --- --- --- --- --- --- --- ---


    // Добавление на склад
    public void addStock(int bookId, int qty) {
        if (!catalog.containsKey(bookId))
            throw new IllegalArgumentException("Книга не существует в каталоге: " + bookId);

        int cur = stock.getOrDefault(bookId, 0);
        stock.put(bookId, cur + qty);
        arrivalDates.put(bookId, LocalDate.now());
        catalog.get(bookId).setStatus(Book.Status.AVAILABLE);

        System.out.println("[model.Inventory] Добавлено на склад: " + bookId + ", qty=" + qty + " (новое количество=" + (cur + qty) + ")");
    }

    // Списание со склада (продажа)
    public void reduceStock(int bookId, int qty) {
        if (!catalog.containsKey(bookId)) return;

        int cur = stock.getOrDefault(bookId, 0);
        int rem = Math.max(0, cur - qty);
        stock.put(bookId, rem);

        if (rem == 0) {
            Book b = catalog.get(bookId);
            if (b != null) b.setStatus(Book.Status.ABSENT);
        }

        lastSoldDate.put(bookId, LocalDate.now());
        System.out.println("[model.Inventory] Списание со склада: " + bookId + ", qty=" + qty + " (остаток=" + rem + ")");
    }

    // Полное списание книги (удаление с полки)
    public void writeOff(int bookId) {
        if (!catalog.containsKey(bookId)) return;

        stock.put(bookId, 0);
        Book b = catalog.get(bookId);
        if (b != null) b.setStatus(Book.Status.ABSENT);

        System.out.println("[model.Inventory] Книга списана: " + bookId);
    }

    // =================== ВЫБОРКИ / АНАЛИТИКА ===================

    // 1️⃣ Все книги по алфавиту (только доступные)
    public List<Book> listBooksByTitle() {
        return catalog.values().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparing(Book::getTitle))
                .collect(Collectors.toList());
    }

    // 2️⃣ По дате поступления
    public List<Book> listBooksByArrivalDate() {
        return catalog.values().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparing(b -> arrivalDates.getOrDefault(b.getId(), LocalDate.MIN)))
                .collect(Collectors.toList());
    }

    // 3️⃣ По цене
    public List<Book> listBooksByPrice() {
        return catalog.values().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparingDouble(b -> prices.getOrDefault(b.getId(), 0.0)))
                .collect(Collectors.toList());
    }

    // 4️⃣ По наличию
    public List<Book> listBooksByAvailability() {
        return catalog.values().stream()
                .filter(b -> b.getStatus() == Book.Status.AVAILABLE)
                .sorted(Comparator.comparing(Book::getStatus))
                .collect(Collectors.toList());
    }

    // 5️⃣ Залежалые книги (не продавались X месяцев) - теперь использует значение из конфига
    public List<Book> listStaleBooks() {
        return listStaleBooks(this.staleMonthsThreshold);
    }

    // Перегруженный метод для совместимости
    public List<Book> listStaleBooks(int months) {
        LocalDate cutoff = LocalDate.now().minusMonths(months);
        return catalog.values().stream()
                .filter(b -> {
                    LocalDate lastSold = lastSoldDate.getOrDefault(b.getId(), arrivalDates.get(b.getId()));
                    return lastSold.isBefore(cutoff);
                })
                .sorted(Comparator.comparing(b -> arrivalDates.get(b.getId())))
                .collect(Collectors.toList());
    }
    public Map<Integer, Integer> getStockMap() {
        return new HashMap<>(stock);
    }

    public Map<Integer, LocalDate> getArrivalDatesMap() {
        return new HashMap<>(arrivalDates);
    }

    public Map<Integer, Double> getPriceMap() {
        return new HashMap<>(prices);
    }

    public Map<Integer, LocalDate> getLastSoldDateMap() {
        return new HashMap<>(lastSoldDate);
    }
    public void restoreFromState(model.persistence.AppState state) {
        this.catalog.clear();
        this.catalog.putAll(state.getCatalog());

        this.stock.clear();
        this.stock.putAll(state.getStock());

        this.arrivalDates.clear();
        this.arrivalDates.putAll(state.getArrivalDates());

        this.prices.clear();
        this.prices.putAll(state.getPrices());

        this.lastSoldDate.clear();
        this.lastSoldDate.putAll(state.getLastSoldDate());

        // Восстанавливаем конфигурацию из состояния
        this.staleMonthsThreshold = state.getStaleMonthsThreshold();
        this.autoCloseRequestsOnStockAdd = state.isAutoCloseRequestsOnStockAdd();

        System.out.println("[model.Inventory] Состояние инвентаря восстановлено.");
    }

}