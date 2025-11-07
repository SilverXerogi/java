package model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Inventory {
    private final Map<String, Book> catalog = new HashMap<>();     // bookId -> model.Book
    private final Map<String, Integer> stock = new HashMap<>();    // bookId -> quantity
    private final Map<String, LocalDate> arrivalDates = new HashMap<>(); // bookId -> дата поступления
    private final Map<String, Double> prices = new HashMap<>();    // bookId -> цена
    private final Map<String, LocalDate> lastSoldDate = new HashMap<>(); // bookId -> дата последней продажи

    // Добавить книгу в каталог
    public void registerBook(Book book, int initialQty, double price) {
        catalog.put(book.getId(), book);
        stock.put(book.getId(), Math.max(0, initialQty));
        prices.put(book.getId(), price);
        arrivalDates.put(book.getId(), LocalDate.now());

        if (initialQty > 0) {
            book.setStatus(Book.Status.AVAILABLE);
        } else {
            book.setStatus(Book.Status.ABSENT);
        }

        System.out.println("[model.Inventory] Зарегистрирована книга: " + book + ", qty=" + initialQty + ", price=" + price);
    }
    public Map<String, Book> getCatalogMap() {
        return new HashMap<>(catalog);
    }

    public Book getBook(String bookId) {
        return catalog.get(bookId);
    }

    public int getQuantity(String bookId) {
        return stock.getOrDefault(bookId, 0);
    }

    public double getPrice(String bookId) {
        return prices.getOrDefault(bookId, 0.0);
    }

    public LocalDate getArrivalDate(String bookId) {
        return arrivalDates.get(bookId);
    }

    // Добавление на склад
    public void addStock(String bookId, int qty) {
        if (!catalog.containsKey(bookId))
            throw new IllegalArgumentException("Книга не существует в каталоге: " + bookId);

        int cur = stock.getOrDefault(bookId, 0);
        stock.put(bookId, cur + qty);
        arrivalDates.put(bookId, LocalDate.now()); // обновляем дату поступления
        catalog.get(bookId).setStatus(Book.Status.AVAILABLE);

        System.out.println("[model.Inventory] Добавлено на склад: " + bookId + ", qty=" + qty + " (новое количество=" + (cur + qty) + ")");
    }

    // Списание со склада
    public void reduceStock(String bookId, int qty) {
        int cur = stock.getOrDefault(bookId, 0);
        int rem = Math.max(0, cur - qty);
        stock.put(bookId, rem);

        if (rem == 0) {
            Book b = catalog.get(bookId);
            if (b != null) b.setStatus(Book.Status.ABSENT);
        }

        lastSoldDate.put(bookId, LocalDate.now()); // фиксируем дату продажи
        System.out.println("[model.Inventory] Списание со склада: " + bookId + ", qty=" + qty + " (остаток=" + rem + ")");
    }

    // Списание (перевод в отсутствует)
    public void writeOff(String bookId) {
        stock.put(bookId, 0);
        Book b = catalog.get(bookId);
        if (b != null) b.setStatus(Book.Status.ABSENT);
        System.out.println("[model.Inventory] Книга списана: " + bookId);
    }

    // =================== ВЫБОРКИ / АНАЛИТИКА ===================

    // 1️⃣ Все книги, отсортированные по названию (алфавиту)
    public List<Book> listBooksByTitle() {
        return catalog.values().stream()
                .sorted(Comparator.comparing(Book::getTitle))
                .collect(Collectors.toList());
    }

    // 2️⃣ Сортировка по дате поступления
    public List<Book> listBooksByArrivalDate() {
        return catalog.values().stream()
                .sorted(Comparator.comparing(b -> arrivalDates.getOrDefault(b.getId(), LocalDate.MIN)))
                .collect(Collectors.toList());
    }

    // 3️⃣ Сортировка по цене
    public List<Book> listBooksByPrice() {
        return catalog.values().stream()
                .sorted(Comparator.comparingDouble(b -> prices.getOrDefault(b.getId(), 0.0)))
                .collect(Collectors.toList());
    }

    // 4️⃣ Сортировка по наличию (AVAILABLE первыми)
    public List<Book> listBooksByAvailability() {
        return catalog.values().stream()
                .sorted(Comparator.comparing(Book::getStatus))
                .collect(Collectors.toList());
    }


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

}
