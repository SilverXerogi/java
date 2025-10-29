
import java.util.*;

public class BookStoreServiceImpl implements BookStoreService {
    private final Inventory inventory;
    private final Map<String, Order> orders = new HashMap<>();
    private final Map<String, BookRequest> requests = new HashMap<>();
    // map bookId -> list of open request ids
    private final Map<String, List<String>> requestsByBook = new HashMap<>();

    public BookStoreServiceImpl(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Order createOrder(Map<String, Integer> items) {
        System.out.println("[Service] Создание заказа: " + items);
        Order order = new Order(items);
        orders.put(order.getId(), order);

        // For any absent book in order, create request automatically
        for (String bookId : items.keySet()) {
            Book book = inventory.getBook(bookId);
            if (book == null) {
                System.out.println("[Service] В каталоге нет книги с id=" + bookId + ". Рекомендуется зарегистрировать книгу.");
                // still create request
                createRequestIfAbsent(bookId);
            } else if (book.getStatus() == Book.Status.ABSENT) {
                System.out.println("[Service] Книга '" + book.getTitle() + "' отсутствует — автоматически создаём запрос.");
                createRequestIfAbsent(bookId);
            }
        }

        System.out.println("[Service] Заказ создан с id=" + order.getId() + ". Статус: " + order.getStatus());
        return order;
    }

    private BookRequest createRequestIfAbsent(String bookId) {
        // If there's already an open request for this book, reuse it
        List<String> list = requestsByBook.getOrDefault(bookId, new ArrayList<>());
        for (String reqId : list) {
            BookRequest r = requests.get(reqId);
            if (r != null && r.getStatus() == BookRequest.Status.OPEN) {
                System.out.println("[Service] Открытый запрос уже существует для bookId=" + bookId + ", requestId=" + r.getId());
                return r;
            }
        }
        BookRequest r = new BookRequest(bookId);
        requests.put(r.getId(), r);
        requestsByBook.computeIfAbsent(bookId, k -> new ArrayList<>()).add(r.getId());
        System.out.println("[Service] Создан запрос на книгу: " + r);
        return r;
    }

    @Override
    public void cancelOrder(String orderId) {
        Order o = orders.get(orderId);
        if (o == null) {
            System.out.println("[Service] Невозможно отменить — заказ не найден: " + orderId);
            return;
        }
        o.setStatus(Order.Status.CANCELED);
        System.out.println("[Service] Заказ отменён: " + o);
    }

    @Override
    public void changeOrderStatus(String orderId, Order.Status status) {
        Order o = orders.get(orderId);
        if (o == null) {
            System.out.println("[Service] Заказ не найден: " + orderId);
            return;
        }
        if (status == Order.Status.COMPLETED) {
            // check availability
            boolean allAvailable = true;
            for (Map.Entry<String,Integer> e : o.getItems().entrySet()) {
                String bookId = e.getKey();
                int qty = e.getValue();
                Book b = inventory.getBook(bookId);
                int have = inventory.getQuantity(bookId);
                if (b == null || b.getStatus() == Book.Status.ABSENT || have < qty) {
                    allAvailable = false;
                    System.out.println("[Service] Нельзя завершить заказ " + orderId + " — книга " + bookId + " недоступна или недостаточно в наличии (нужно " + qty + ", есть " + have + ")");
                }
            }
            if (!allAvailable) {
                System.out.println("[Service] Заказ не может быть переведён в COMPLETED до выполнения всех запросов на отсутствующие книги.");
                return;
            }
            // reduce stock
            for (Map.Entry<String,Integer> e : o.getItems().entrySet()) {
                inventory.reduceStock(e.getKey(), e.getValue());
            }
            o.setStatus(Order.Status.COMPLETED);
            System.out.println("[Service] Заказ успешно завершён: " + o);
            return;
        }
        o.setStatus(status);
        System.out.println("[Service] Статус заказа " + orderId + " изменён на " + status);
    }

    @Override
    public void addBookToInventory(String bookId, int qty) {
        Book book = inventory.getBook(bookId);
        if (book == null) {
            System.out.println("[Service] Попытка добавить книгу на склад, но книга не зарегистрирована в каталоге: " + bookId);
            return;
        }
        inventory.addStock(bookId, qty);
        // Close open requests for this book
        List<String> list = requestsByBook.getOrDefault(bookId, Collections.emptyList());
        for (String reqId : new ArrayList<>(list)) {
            BookRequest r = requests.get(reqId);
            if (r != null && r.getStatus() == BookRequest.Status.OPEN) {
                r.close();
                System.out.println("[Service] Закрыт запрос: " + r.getId() + " для книги " + bookId);
            }
        }
    }

    @Override
    public BookRequest requestBook(String bookId) {
        BookRequest r = createRequestIfAbsent(bookId);
        return r;
    }

    @Override
    public void writeOffBook(String bookId) {
        Book book = inventory.getBook(bookId);
        if (book == null) {
            System.out.println("[Service] Невозможно списать — книга не зарегистрирована: " + bookId);
            return;
        }
        inventory.writeOff(bookId);
        // (optionally) create requests by other logic; here we just mark absent
    }

    // for test/debug convenience
    public Order getOrder(String id) { return orders.get(id); }
    public BookRequest getRequest(String id) { return requests.get(id); }
}
