package bookstore.ui;

import bookstore.model.Book;
import bookstore.model.BookStoreService;
import bookstore.model.Inventory;
import bookstore.model.Order;
import bookstore.model.factory.DefaultBookStoreFactory;
import bookstore.model.io.BookCSVHandler;
import bookstore.model.io.OrderCSVHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MenuFactory {
    private static final Logger logger = LoggerFactory.getLogger(MenuFactory.class);

    private final BookStoreService store;
    private final Inventory inventory;
    private final DefaultBookStoreFactory factory;
    private final Scanner scanner = new Scanner(System.in);

    public MenuFactory(BookStoreService store, Inventory inventory, DefaultBookStoreFactory factory) {
        this.store = store;
        this.inventory = inventory;
        this.factory = factory;
    }

    public Menu createMenu(MenuType type) {
        logger.info("Начало обработки команды: {}", type);
        try {
            Menu menu = buildMenu(type);
            logger.info("Команда успешно обработана: {}", type);
            return menu;
        } catch (Exception e) {
            logger.error("Ошибка обработки команды: {}", type, e);
            throw e;
        }
    }

    private Menu buildMenu(MenuType type) {
        switch (type) {
            case BOOKS:
                return buildBooksMenu();
            case ORDERS:
                return buildOrdersMenu();
            case REPORTS:
                return buildReportsMenu();
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

    private Menu buildBooksMenu() {
        return new Menu("Меню книг", Arrays.asList(
                new MenuItem("Показать все книги", () -> {
                    logger.info("Начало команды: Показать все книги");
                    try {
                        List<Book> books = store.getBooksSortedByTitle();
                        if (books == null || books.isEmpty()) {
                            System.out.println("Книг нет.");
                            return;
                        }
                        books.forEach(b -> System.out.println(
                                b.getId() + " — " + b.getTitle() +
                                        " (" + b.getPrice() + " руб, " + b.getStatus() + ")"));
                        logger.info("Команда 'Показать все книги' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Показать все книги'", e);
                    }
                }, null),

                new MenuItem("Добавить книгу", () -> {
                    logger.info("Начало команды: Добавить книгу");
                    try {
                        System.out.print("Название книги: ");
                        String title = scanner.nextLine().trim();
                        if (title.isEmpty()) {
                            System.out.println("Название не может быть пустым.");
                            return;
                        }
                        double price = readDouble("Цена книги: ");

                        Book book = factory.createBook(title, Book.Status.AVAILABLE, price, LocalDate.now(), "");
                        inventory.registerBook(book, 1, price);
                        System.out.println("Книга добавлена: " + book.getTitle() + " (ID=" + book.getId() + ")");
                        logger.info("Команда 'Добавить книгу' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Добавить книгу'", e);
                    }
                }, null),

                new MenuItem("Списать книгу по ID", () -> {
                    logger.info("Начало команды: Списать книгу по ID");
                    try {
                        int bookId = readInt("Введите ID книги для списания: ");
                        Book book = store.getBookDetails(bookId);
                        if (book == null) {
                            System.out.println("Книга с таким ID не найдена.");
                            return;
                        }
                        store.writeOffBook(bookId);
                        System.out.println("Списана книга: " + book.getTitle() + " (ID=" + book.getId() + ")");
                        logger.info("Команда 'Списать книгу по ID' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Списать книгу по ID'", e);
                    }
                }, null),

                new MenuItem("Экспорт книг в CSV", () -> {
                    logger.info("Начало команды: Экспорт книг в CSV");
                    try {
                        new BookCSVHandler(inventory).exportBooks("books.csv");
                        System.out.println("✅ Книги экспортированы в books.csv");
                        logger.info("Команда 'Экспорт книг в CSV' выполнена");
                    } catch (IOException e) {
                        logger.error("Ошибка при экспорте книг в CSV", e);
                    }
                }, null),

                new MenuItem("Импорт книг из CSV", () -> {
                    logger.info("Начало команды: Импорт книг из CSV");
                    try {
                        new BookCSVHandler(inventory).importBooks("books.csv");
                        System.out.println("✅ Книги импортированы из books.csv");
                        logger.info("Команда 'Импорт книг из CSV' выполнена");
                    } catch (IOException e) {
                        logger.error("Ошибка при импорте книг из CSV", e);
                    }
                }, null)
        ));
    }

    private Menu buildOrdersMenu() {
        return new Menu("Меню заказов", Arrays.asList(
                new MenuItem("Создать заказ", () -> {
                    logger.info("Начало команды: Создать заказ");
                    try {
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
                                            ", qty: " + inventory.getQuantity(b.getId()) + ")"));

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
                        logger.info("Команда 'Создать заказ' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Создать заказ'", e);
                    }
                }, null),

                new MenuItem("Список заказов по дате", () -> {
                    logger.info("Начало команды: Список заказов по дате");
                    try {
                        store.getOrdersSortedByDate().forEach(System.out::println);
                        logger.info("Команда 'Список заказов по дате' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Список заказов по дате'", e);
                    }
                }, null),

                new MenuItem("Список заказов по цене", () -> {
                    logger.info("Начало команды: Список заказов по цене");
                    try {
                        store.getOrdersSortedByPrice().forEach(System.out::println);
                        logger.info("Команда 'Список заказов по цене' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Список заказов по цене'", e);
                    }
                }, null),

                new MenuItem("Список заказов по статусу", () -> {
                    logger.info("Начало команды: Список заказов по статусу");
                    try {
                        store.getOrdersSortedByStatus().forEach(System.out::println);
                        logger.info("Команда 'Список заказов по статусу' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Список заказов по статусу'", e);
                    }
                }, null),

                new MenuItem("Экспорт заказов в CSV", () -> {
                    logger.info("Начало команды: Экспорт заказов в CSV");
                    try {
                        new OrderCSVHandler(store.getAllOrders(), inventory).exportOrders("orders.csv");
                        System.out.println("✅ Заказы экспортированы в orders.csv");
                        logger.info("Команда 'Экспорт заказов в CSV' выполнена");
                    } catch (IOException e) {
                        logger.error("Ошибка при экспорте заказов в CSV", e);
                    }
                }, null),

                new MenuItem("Импорт заказов из CSV", () -> {
                    logger.info("Начало команды: Импорт заказов из CSV");
                    try {
                        new OrderCSVHandler(store.getAllOrders(), inventory).importOrders("orders.csv");
                        System.out.println("✅ Заказы импортированы из orders.csv");
                        logger.info("Команда 'Импорт заказов из CSV' выполнена");
                    } catch (IOException e) {
                        logger.error("Ошибка при импорте заказов из CSV", e);
                    }
                }, null)
        ));
    }

    private Menu buildReportsMenu() {
        return new Menu("Отчёты", Arrays.asList(
                new MenuItem("Залежавшиеся книги (" + inventory.getStaleMonthsThreshold() + " мес)", () -> {
                    logger.info("Начало команды: Залежавшиеся книги");
                    try {
                        inventory.listStaleBooks().forEach(System.out::println);
                        logger.info("Команда 'Залежавшиеся книги' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Залежавшиеся книги'", e);
                    }
                }, null),
                new MenuItem("Выручка за последний месяц", () -> {
                    logger.info("Начало команды: Выручка за последний месяц");
                    try {
                        LocalDate now = LocalDate.now();
                        LocalDate from = now.minusMonths(1);
                        System.out.println("Выручка за последний месяц: " +
                                store.getRevenueInPeriod(from, now) + " руб.");
                        logger.info("Команда 'Выручка за последний месяц' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Выручка за последний месяц'", e);
                    }
                }, null),

                new MenuItem("Количество выполненных заказов за последний месяц", () -> {
                    logger.info("Начало команды: Количество выполненных заказов за последний месяц");
                    try {
                        LocalDate now = LocalDate.now();
                        LocalDate from = now.minusMonths(1);
                        System.out.println("Выполнено заказов за последний месяц: " +
                                store.getCompletedOrdersCountInPeriod(from, now));
                        logger.info("Команда 'Количество выполненных заказов за последний месяц' выполнена");
                    } catch (Exception e) {
                        logger.error("Ошибка в команде 'Количество выполненных заказов за последний месяц'", e);
                    }
                }, null)
        ));
    }

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