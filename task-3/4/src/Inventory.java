
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    // catalog: bookId -> Book (metadata)
    private final Map<String, Book> catalog = new HashMap<>();
    // stock: bookId -> quantity on shelf
    private final Map<String, Integer> stock = new HashMap<>();

    // add book definition (meta) to catalog
    public void registerBook(Book book, int initialQty) {
        catalog.put(book.getId(), book);
        stock.put(book.getId(), Math.max(0, initialQty));
        if (initialQty > 0) {
            book.setStatus(Book.Status.AVAILABLE);
        } else {
            book.setStatus(Book.Status.ABSENT);
        }
        System.out.println("[Inventory] Зарегистрирована книга: " + book + ", qty=" + initialQty);
    }

    public Book getBook(String bookId) {
        return catalog.get(bookId);
    }

    public int getQuantity(String bookId) {
        return stock.getOrDefault(bookId, 0);
    }

    public void addStock(String bookId, int qty) {
        if (!catalog.containsKey(bookId)) throw new IllegalArgumentException("Книга не существует в каталоге: " + bookId);
        int cur = stock.getOrDefault(bookId, 0);
        stock.put(bookId, cur + qty);
        catalog.get(bookId).setStatus(Book.Status.AVAILABLE);
        System.out.println("[Inventory] Добавлено на склад: bookId=" + bookId + ", qty=" + qty + " (новоe количество=" + (cur + qty) + ")");
    }

    // reduce stock when order completed or write-off
    public void reduceStock(String bookId, int qty) {
        int cur = stock.getOrDefault(bookId, 0);
        int rem = Math.max(0, cur - qty);
        stock.put(bookId, rem);
        if (rem == 0) {
            Book b = catalog.get(bookId);
            if (b != null) b.setStatus(Book.Status.ABSENT);
        }
        System.out.println("[Inventory] Списание со склада: bookId=" + bookId + ", qty=" + qty + " (остаток=" + rem + ")");
    }

    // mark absent explicitly (write off)
    public void writeOff(String bookId) {
        stock.put(bookId, 0);
        Book b = catalog.get(bookId);
        if (b != null) b.setStatus(Book.Status.ABSENT);
        System.out.println("[Inventory] Книга списана (переведена в статус ABSENT): " + bookId);
    }
}
