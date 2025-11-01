import java.time.LocalDate;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // ------------------- Подготовка инвентаря -------------------
        Inventory inventory = new Inventory();
        Book book1 = new Book("B1", "Clean Code", Book.Status.AVAILABLE, 45.0,
                LocalDate.of(2008, 8, 1), "Programming book");
        Book book2 = new Book("B2", "Effective Java", Book.Status.AVAILABLE, 50.0,
                LocalDate.of(2018, 1, 6), "Java best practices");
        Book book3 = new Book("B3", "Алгоритмы", Book.Status.AVAILABLE, 40.0,
                LocalDate.of(2015, 5, 15), "Algorithms textbook");

        inventory.registerBook(book1, 5, 45.0);
        inventory.registerBook(book2, 2, 50.0);
        inventory.registerBook(book3, 0, 40.0); // изначально отсутствует

        BookStoreServiceImpl service = new BookStoreServiceImpl(inventory);

        // ------------------- Создание заказов -------------------
        Map<String, Integer> items1 = new HashMap<>();
        items1.put("B3", 1); // отсутствует
        Order order1 = service.createOrder(items1);
        order1.setCustomerName("Иван Иванов");

        Map<String, Integer> items2 = new HashMap<>();
        items2.put("B1", 2);
        Order order2 = service.createOrder(items2);
        order2.setCustomerName("Пётр Петров");

        Map<String, Integer> items3 = new HashMap<>();
        items3.put("B2", 1);
        Order order3 = service.createOrder(items3);
        order3.setCustomerName("Анна Антонова");

        // Попытка завершить заказ 1 (не выполнен, книга отсутствует)
        service.changeOrderStatus(order1.getId(), Order.Status.COMPLETED);

        // Добавляем книгу B3 на склад
        service.addBookToInventory("B3", 3);

        // Завершаем заказ 1 после пополнения
        service.changeOrderStatus(order1.getId(), Order.Status.COMPLETED);

        // Завершаем остальные заказы
        service.changeOrderStatus(order2.getId(), Order.Status.COMPLETED);
        service.changeOrderStatus(order3.getId(), Order.Status.COMPLETED);

        // ------------------- Вывод информации -------------------

        System.out.println("\n--- Список книг по алфавиту ---");
        for (Book b : service.listBooksSortedByTitle()) System.out.println(b);

        System.out.println("\n--- Список книг по дате поступления ---");
        for (Book b : service.listBooksSortedByDate()) System.out.println(b);

        System.out.println("\n--- Список книг по цене ---");
        for (Book b : service.listBooksSortedByPrice()) System.out.println(b);

        System.out.println("\n--- Список книг по наличию ---");
        for (Book b : service.listBooksSortedByAvailability()) System.out.println(b);

        System.out.println("\n--- Список заказов по дате ---");
        for (Order o : service.listOrdersSortedByDate()) System.out.println(o);

        System.out.println("\n--- Список заказов по цене ---");
        for (Order o : service.listOrdersSortedByPrice()) System.out.println(o);

        System.out.println("\n--- Список заказов по статусу ---");
        for (Order o : service.listOrdersSortedByStatus()) System.out.println(o);

        System.out.println("\n--- Список запросов по количеству ---");
        for (BookRequest r : service.listRequestsSortedByRequestCount()) System.out.println(r);

        System.out.println("\n--- Список запросов по алфавиту книг ---");
        for (BookRequest r : service.listRequestsSortedByTitle()) System.out.println(r);

        // Период для отчёта
        Date from = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30); // 30 дней назад
        Date to = new Date();

        System.out.println("\n--- Выполненные заказы за период ---");
        for (Order o : service.listCompletedOrdersInPeriod(from, to)) System.out.println(o);

        System.out.println("\n--- Сумма заработанных средств за период ---");
        System.out.println(service.getRevenueInPeriod(from, to));

        System.out.println("\n--- Количество выполненных заказов за период ---");
        System.out.println(service.getCompletedOrdersCountInPeriod(from, to));

        System.out.println("\n--- Список залежавшихся книг (6 месяцев) ---");
        for (Book b : service.listStaleBooks(6)) System.out.println(b);

        System.out.println("\n--- Детали заказа ---");
        System.out.println(service.getOrderDetails(order1.getId()));

        System.out.println("\n--- Описание книги ---");
        System.out.println(service.getBookDetails("B1").getDescription());

        System.out.println("\n--- Конец демонстрации ---");
    }
}
