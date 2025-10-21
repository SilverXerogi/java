
public interface BookStoreService {
    Order createOrder(java.util.Map<String,Integer> items); // items: bookId -> qty
    void cancelOrder(String orderId);
    void changeOrderStatus(String orderId, Order.Status status);
    void addBookToInventory(String bookId, int qty);
    BookRequest requestBook(String bookId);
    void writeOffBook(String bookId);
}
