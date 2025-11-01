import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BookStoreService {

    // --- Работа с заказами ---
    Order createOrder(Map<String, Integer> items); // items: bookId -> qty
    void cancelOrder(String orderId);
    void changeOrderStatus(String orderId, Order.Status status);

    // --- Работа с книгами ---
    void addBookToInventory(String bookId, int qty);
    void writeOffBook(String bookId);
    BookRequest requestBook(String bookId);

    // --- Просмотр данных и сортировка ---
    List<Book> listBooksSortedByTitle();
    List<Book> listBooksSortedByPrice();
    List<Book> listBooksSortedByDate();
    List<Book> listBooksSortedByAvailability();

    List<Order> listOrdersSortedByDate();
    List<Order> listOrdersSortedByPrice();
    List<Order> listOrdersSortedByStatus();

    List<BookRequest> listRequestsSortedByTitle();
    List<BookRequest> listRequestsSortedByRequestCount();

    List<Order> listCompletedOrdersInPeriod(Date from, Date to);
    double getRevenueInPeriod(Date from, Date to);
    int getCompletedOrdersCountInPeriod(Date from, Date to);

    List<Book> listStaleBooks(int months); // книги не проданы более чем months месяцев

    // --- Детали ---
    Order getOrderDetails(String orderId);
    Book getBookDetails(String bookId);
}
