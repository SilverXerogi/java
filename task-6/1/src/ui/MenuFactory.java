package ui;

import model.*;
import model.factory.DefaultBookStoreFactory;
import model.io.BookCSVHandler;
import model.io.OrderCSVHandler;
import model.io.BookRequestCSVHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class MenuFactory {
    private final BookStoreService store;
    private final Scanner scanner = new Scanner(System.in);

    public MenuFactory(BookStoreService store) {
        this.store = store;
    }

    public Menu createMenu(MenuType type) {
        switch (type) {
            case BOOKS:
                return new Menu("Меню книг", Arrays.asList(
                        new MenuItem("Показать все книги", () -> {
                            List<Book> books = store.getBooksSortedByTitle();
                            if (books == null || books.isEmpty()) {
                                System.out.println("Книг нет.");
                                return;
                            }
                            books.forEach(b -> System.out.println(
                                    b.getId() + " — " + b.getTitle() +
                                            " (" + b.getPrice() + " руб, " + b.getStatus() + ")"));
                        }, null),

                        new MenuItem("Добавить книгу", () -> {
                            System.out.print("Название книги: ");
                            String title = scanner.nextLine().trim();
                            if (title.isEmpty()) {
                                System.out.println("Название не может быть пустым.");
                                return;
                            }
                            double price = readDouble("Цена книги: ");

                            Book book = DefaultBookStoreFactory.getInstance()
                                    .createBook(title, Book.Status.AVAILABLE, price, LocalDate.now(), "");
                            Inventory.getInstance().registerBook(book, 1, price);
                            System.out.println("Книга добавлена: " + book.getTitle() + " (ID=" + book.getId() + ")");
                        }, null),

                        new MenuItem("Списать книгу по ID", () -> {
                            int bookId = readInt("Введите ID книги для списания: ");
                            Book book = store.getBookDetails(bookId);
                            if (book == null) {
                                System.out.println("Книга с таким ID не найдена.");
                                return;
                            }
                            store.writeOffBook(bookId);
                            System.out.println("Списана книга: " + book.getTitle() + " (ID=" + book.getId() + ")");
                        }, null),

                        new MenuItem("Экспорт книг в CSV", () -> {
                            try {
                                new BookCSVHandler().exportBooks("books.csv");
                                System.out.println("✅ Книги экспортированы в books.csv");
                            } catch (IOException e) {
                                System.out.println("Ошибка при экспорте: " + e.getMessage());
                            }
                        }, null),

                        new MenuItem("Импорт книг из CSV", () -> {
                            try {
                                new BookCSVHandler().importBooks("books.csv");
                                System.out.println("✅ Книги импортированы из books.csv");
                            } catch (IOException e) {
                                System.out.println("Ошибка при импорте: " + e.getMessage());
                            }
                        }, null)
                ));

            case ORDERS:
                return new Menu("Меню заказов", Arrays.asList(
                        new MenuItem("Создать заказ", () -> {
                            Map<Integer, Integer> items = new LinkedHashMap<>();
                            while (true) {
                                List<Book> books = store.getBooksSortedByTitle();
                                if (books == null || books.isEmpty()) {
                                    System.out.println("Нет книг в каталоге.");
                                    break;
                                }
                                System.out.println("\nДоступные книги:");
                                books.forEach(b -> System.out.println(
                                        b.getId() + " — " + b.getTitle() +
                                                " (цена: " + b.getPrice() +
                                                " руб, статус: " + b.getStatus() +
                                                ", qty: " + Inventory.getInstance().getQuantity(b.getId()) + ")"));

                                System.out.print("ID книги (Enter для завершения выбора): ");
                                String line = scanner.nextLine().trim();
                                if (line.isEmpty()) break;

                                int bookId;
                                try {
                                    bookId = Integer.parseInt(line);
                                } catch (NumberFormatException e) {
                                    System.out.println("Некорректный ID.");
                                    continue;
                                }

                                Book book = store.getBookDetails(bookId);
                                if (book == null) {
                                    System.out.println("Книга с таким ID не найдена.");
                                    continue;
                                }
                                if (book.getStatus() == Book.Status.ABSENT) {
                                    System.out.println("Книга отсутствует на складе.");
                                    continue;
                                }

                                int qty = readInt("Количество: ");
                                if (qty <= 0) {
                                    System.out.println("Количество должно быть > 0.");
                                    continue;
                                }

                                items.put(bookId, items.getOrDefault(bookId, 0) + qty);
                                System.out.println("Добавлено: " + book.getTitle() + " x" + qty);
                            }

                            if (items.isEmpty()) {
                                System.out.println("Заказ не создан (нет выбранных книг).");
                                return;
                            }

                            System.out.print("Имя покупателя: ");
                            String customer = scanner.nextLine().trim();
                            if (customer.isEmpty()) customer = "Без имени";

                            Order created = store.createOrder(items, customer);
                            System.out.println("Заказ создан. ID заказа: " + created.getId() +
                                    ", сумма: " + created.getTotalPrice() + " руб.");
                        }, null),

                        new MenuItem("Список заказов по дате", () ->
                                store.getOrdersSortedByDate().forEach(System.out::println), null),

                        new MenuItem("Список заказов по цене", () ->
                                store.getOrdersSortedByPrice().forEach(System.out::println), null),

                        new MenuItem("Список заказов по статусу", () ->
                                store.getOrdersSortedByStatus().forEach(System.out::println), null),

                        new MenuItem("Экспорт заказов в CSV", () -> {
                            try {
                                new OrderCSVHandler(store.getAllOrders()).exportOrders("orders.csv");
                                System.out.println("✅ Заказы экспортированы в orders.csv");
                            } catch (IOException e) {
                                System.out.println("Ошибка при экспорте заказов: " + e.getMessage());
                            }
                        }, null),

                        new MenuItem("Импорт заказов из CSV", () -> {
                            try {
                                new OrderCSVHandler(store.getAllOrders()).importOrders("orders.csv");
                                System.out.println("✅ Заказы импортированы из orders.csv");
                            } catch (IOException e) {
                                System.out.println("Ошибка при импорте заказов: " + e.getMessage());
                            }
                        }, null)
                ));

            case REPORTS:
                return new Menu("Отчёты", Arrays.asList(
                        new MenuItem("Залежавшиеся книги (6 мес)", () ->
                                store.getStaleBooks(6).forEach(System.out::println), null),

                        new MenuItem("Выручка за последний месяц", () -> {
                            LocalDate now = LocalDate.now();
                            LocalDate from = now.minusMonths(1);
                            System.out.println("Выручка за последний месяц: " +
                                    store.getRevenueInPeriod(from, now) + " руб.");
                        }, null),

                        new MenuItem("Количество выполненных заказов за последний месяц", () -> {
                            LocalDate now = LocalDate.now();
                            LocalDate from = now.minusMonths(1);
                            System.out.println("Выполнено заказов за последний месяц: " +
                                    store.getCompletedOrdersCountInPeriod(from, now));
                        }, null)
                ));

            case MAIN:
            default:
                Menu booksMenu = createMenu(MenuType.BOOKS);
                Menu ordersMenu = createMenu(MenuType.ORDERS);
                Menu reportsMenu = createMenu(MenuType.REPORTS);
                return new Menu("Главное меню", Arrays.asList(
                        new MenuItem("Работа с книгами", null, booksMenu),
                        new MenuItem("Работа с заказами", null, ordersMenu),
                        new MenuItem("Отчёты", null, reportsMenu),
                        new MenuItem("Выход", () -> System.exit(0), null)
                ));
        }
    }

    // --- Вспомогательные методы ---
    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка! Введите целое число.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка! Введите число.");
            }
        }
    }
}
