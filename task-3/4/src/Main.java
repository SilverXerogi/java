
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Подготовим инвентарь с заранее определёнными книгами
        Inventory inventory = new Inventory();
        Book book1 = new Book("B1", "Clean Code", Book.Status.AVAILABLE);
        Book book2 = new Book("B2", "Effective Java", Book.Status.AVAILABLE);
        Book book3 = new Book("B3", "Алгоритмы", Book.Status.ABSENT); // изначально отсутствует

        inventory.registerBook(book1, 5);
        inventory.registerBook(book2, 2);
        inventory.registerBook(book3, 0);

        BookStoreServiceImpl service = new BookStoreServiceImpl(inventory);

        // 1) Создаём заказ на книгу, которой нет в наличии -> автоматически создаётся запрос
        Map<String, Integer> items1 = new HashMap<>();
        items1.put("B3", 1); // отсутствует
        Order order1 = service.createOrder(items1);

        // Попытка завершить заказ пока не выполнен запрос
        service.changeOrderStatus(order1.getId(), Order.Status.COMPLETED); // должен отказать

        // 2) Добавляем книгу на склад -> это закроет запрос
        service.addBookToInventory("B3", 3);

        // После поставки можно попытаться завершить заказ снова
        service.changeOrderStatus(order1.getId(), Order.Status.COMPLETED);

        // 3) Создаём заказ, затем списываем книгу со склада
        Map<String, Integer> items2 = new HashMap<>();
        items2.put("B2", 1);
        Order order2 = service.createOrder(items2);
        service.changeOrderStatus(order2.getId(), Order.Status.COMPLETED); // успешно

        // Списываем B2 со склада
        service.writeOffBook("B2");

        // 4) Попытка создать заказ на незарегистрированную книгу (B999)
        Map<String, Integer> items3 = new HashMap<>();
        items3.put("B999", 1);
        Order order3 = service.createOrder(items3); // создаст запрос и предупредит

        // Отмена заказа
        service.cancelOrder(order3.getId());

        System.out.println("\n--- Конец демонстрации ---");
    }
}
