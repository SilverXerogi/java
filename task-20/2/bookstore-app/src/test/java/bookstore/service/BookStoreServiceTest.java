package bookstore.service;

import bookstore.dao.BookDAO;
import bookstore.dao.BookRequestDAO;
import bookstore.dao.OrderDAO;
import bookstore.entity.BookEntity;
import bookstore.entity.BookRequestEntity;
import bookstore.entity.OrderEntity;
import bookstore.model.Book;
import bookstore.model.BookRequest;
import bookstore.model.Inventory;
import bookstore.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookStoreServiceTest {

    @Mock
    private BookDAO bookDAO;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private BookRequestDAO requestDAO;

    @Mock
    private Inventory inventory;

    @InjectMocks
    private bookstore.model.BookStoreServiceImpl bookStoreService; // ✅ Полный путь к классу

    @BeforeEach
    void setUp() {
        // Mockito автоматически вставит mocked зависимости через @InjectMocks
        // НЕ НУЖНО вручную присваивать: bookStoreService.inventory = inventory;
    }

    // --- BookService ---

    @Test
    void addBookToInventory_Positive_AddsStock() {
        // Arrange
        int bookId = 1;
        int qty = 10;
        BookEntity mockBook = new BookEntity();
        mockBook.setId(bookId);
        when(bookDAO.findById(bookId)).thenReturn(mockBook);

        // Act
        bookStoreService.addBookToInventory(bookId, qty);

        // Assert
        verify(inventory).addStock(bookId, qty);
        verify(bookDAO).findById(bookId);
    }

    @Test
    void addBookToInventory_Negative_BookNotFound_DoesNotAddStock() {
        // Arrange
        int bookId = 999;
        int qty = 10;
        when(bookDAO.findById(bookId)).thenReturn(null);

        // Act
        bookStoreService.addBookToInventory(bookId, qty);

        // Assert
        verify(inventory, never()).addStock(anyInt(), anyInt());
        verify(bookDAO).findById(bookId);
    }

    @Test
    void writeOffBook_Positive_CallsInventoryWriteOff() {
        // Arrange
        int bookId = 1;
        BookEntity mockBook = new BookEntity();
        mockBook.setId(bookId);
        when(bookDAO.findById(bookId)).thenReturn(mockBook);

        // Act
        bookStoreService.writeOffBook(bookId);

        // Assert
        verify(inventory).writeOff(bookId);
        verify(bookDAO).findById(bookId);
    }

    @Test
    void writeOffBook_Negative_BookNotFound_DoesNotCallWriteOff() {
        // Arrange
        int bookId = 999;
        when(bookDAO.findById(bookId)).thenReturn(null);

        // Act
        bookStoreService.writeOffBook(bookId);

        // Assert
        verify(inventory, never()).writeOff(anyInt());
        verify(bookDAO).findById(bookId);
    }

    @Test
    void getBookDetails_Positive_ReturnsBook() {
        // Arrange
        int bookId = 1;
        BookEntity mockBook = new BookEntity();
        mockBook.setId(1);
        mockBook.setTitle("Test Book");
        mockBook.setStatus(BookEntity.Status.AVAILABLE); // ✅ Правильный enum
        when(bookDAO.findById(bookId)).thenReturn(mockBook);

        // Act
        Book result = bookStoreService.getBookDetails(bookId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookDAO).findById(bookId);
    }

    @Test
    void getBookDetails_Negative_BookNotFound_ReturnsNull() {
        // Arrange
        int bookId = 999;
        when(bookDAO.findById(bookId)).thenReturn(null);

        // Act
        Book result = bookStoreService.getBookDetails(bookId);

        // Assert
        assertNull(result);
        verify(bookDAO).findById(bookId);
    }

    @Test
    void getBooksSortedByTitle_Positive_ReturnsBooks() {
        // Arrange
        List<BookEntity> mockEntities = new ArrayList<>();
        BookEntity book1 = new BookEntity();
        book1.setId(1);
        book1.setTitle("Book A");
        book1.setStatus(BookEntity.Status.AVAILABLE); // ✅ Правильный enum
        BookEntity book2 = new BookEntity();
        book2.setId(2);
        book2.setTitle("Book B");
        book2.setStatus(BookEntity.Status.AVAILABLE); // ✅ Правильный enum
        mockEntities.add(book2);
        mockEntities.add(book1);
        when(bookDAO.findAll()).thenReturn(mockEntities);

        // Act
        List<Book> result = bookStoreService.getBooksSortedByTitle();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Book A", result.get(0).getTitle());
        assertEquals("Book B", result.get(1).getTitle());
        verify(bookDAO).findAll();
    }

    // --- OrderService ---

    @Test
    void createOrder_Positive_CreatesOrder() {
        // Arrange
        int bookId = 1;
        int qty = 2;
        String customer = "Test Customer";
        Book mockBook = new Book(1, "Test Book", Book.Status.AVAILABLE, 100.0, java.time.LocalDate.now(), "Desc");
        when(inventory.getBook(bookId)).thenReturn(mockBook);
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1);
        savedOrder.setItems(Map.of(bookId, qty));
        savedOrder.setCustomerName(customer);
        when(orderDAO.save(any(OrderEntity.class))).thenReturn(savedOrder);

        // Act
        Order result = bookStoreService.createOrder(Map.of(bookId, qty), customer);

        // Assert
        assertNotNull(result);
        assertEquals(customer, result.getCustomerName());
        verify(orderDAO).save(any(OrderEntity.class));
    }

    @Test
    void cancelOrder_Positive_CancelsOrder() {
        // Arrange
        int orderId = 1;
        OrderEntity mockOrder = new OrderEntity();
        mockOrder.setId(orderId);
        mockOrder.setStatus(OrderEntity.Status.PENDING); // ✅ Правильный enum: PENDING -> CANCELLED
        when(orderDAO.findById(orderId)).thenReturn(mockOrder);

        // Act
        bookStoreService.cancelOrder(orderId);

        // Assert
        assertEquals(OrderEntity.Status.CANCELLED, mockOrder.getStatus()); // ✅ Правильный enum
        verify(orderDAO).update(mockOrder);
    }

    @Test
    void cancelOrder_Negative_OrderNotFound_DoesNothing() {
        // Arrange
        int orderId = 999;
        when(orderDAO.findById(orderId)).thenReturn(null);

        // Act
        bookStoreService.cancelOrder(orderId);

        // Assert
        verify(orderDAO, never()).update(any(OrderEntity.class));
    }

    @Test
    void changeOrderStatus_ToCompleted_ThrowsIfNotAllBooksAvailable() {
        // Arrange
        int orderId = 1;
        OrderEntity mockOrder = new OrderEntity();
        mockOrder.setId(orderId);
        mockOrder.setItems(Map.of(1, 1));
        mockOrder.setStatus(OrderEntity.Status.PENDING); // ✅ Правильный enum
        when(orderDAO.findById(orderId)).thenReturn(mockOrder);
        when(inventory.getBook(1)).thenReturn(null); // книга недоступна

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookStoreService.changeOrderStatus(orderId, Order.Status.COMPLETED);
        });
        verify(orderDAO, never()).update(any(OrderEntity.class));
    }

    // --- RequestService ---

    @Test
    void requestBook_Positive_CreatesAndReturnsRequest() {
        // Arrange
        int bookId = 1;
        BookRequestEntity savedRequest = new BookRequestEntity();
        savedRequest.setId(1);
        savedRequest.setBookId(bookId);
        savedRequest.setStatus(BookRequestEntity.Status.OPEN); // ✅ Правильный enum
        savedRequest.setRequestCount(1);
        when(requestDAO.save(any(BookRequestEntity.class))).thenReturn(savedRequest);

        // Act
        BookRequest result = bookStoreService.requestBook(bookId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals(BookRequest.Status.OPEN, result.getStatus());
        verify(requestDAO).save(any(BookRequestEntity.class));
    }

    @Test
    void getRequestsSortedByTitle_Positive_ReturnsRequests() {
        // Arrange
        List<BookRequestEntity> mockRequests = new ArrayList<>();
        BookRequestEntity req1 = new BookRequestEntity();
        req1.setId(1);
        req1.setBookId(1);
        req1.setStatus(BookRequestEntity.Status.OPEN); // ✅ Правильный enum
        BookRequestEntity req2 = new BookRequestEntity();
        req2.setId(2);
        req2.setBookId(2);
        req2.setStatus(BookRequestEntity.Status.CLOSED); // ✅ Правильный enum
        mockRequests.add(req2);
        mockRequests.add(req1);

        BookEntity book1 = new BookEntity();
        book1.setId(1);
        book1.setTitle("Book A");
        BookEntity book2 = new BookEntity();
        book2.setId(2);
        book2.setTitle("Book B");

        when(requestDAO.findAll()).thenReturn(mockRequests);
        when(bookDAO.findById(1)).thenReturn(book1);
        when(bookDAO.findById(2)).thenReturn(book2);

        // Act
        List<BookRequest> result = bookStoreService.getRequestsSortedByTitle();

        // Assert
        assertEquals(2, result.size());
        verify(requestDAO).findAll();
    }
}