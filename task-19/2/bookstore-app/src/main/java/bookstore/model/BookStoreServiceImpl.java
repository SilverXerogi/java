package bookstore.model;

import bookstore.dao.BookDAO;
import bookstore.dao.BookRequestDAO;
import bookstore.dao.OrderDAO;
import bookstore.entity.BookEntity;
import bookstore.entity.BookRequestEntity;
import bookstore.entity.OrderEntity;

import bookstore.model.factory.DefaultBookStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookStoreServiceImpl implements BookStoreService {

    @Autowired
    private BookDAO bookDAO;
    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private BookRequestDAO requestDAO;
    @Autowired
    private Inventory inventory;
    @Autowired
    private DefaultBookStoreFactory factory;

    public Inventory getInventory() {
        return inventory;
    }

    // --- BookService ---

    @Override
    @Transactional
    public void addBookToInventory(int bookId, int qty) {
        BookEntity book = bookDAO.findById(bookId);
        if (book == null) return;

        inventory.addStock(bookId, qty);

        if (inventory.isAutoCloseRequestsOnStockAdd()) {
            List<BookRequestEntity> openRequests = requestDAO.findAll().stream()
                    .filter(r -> r.getBookId() == bookId && r.getStatus() == BookRequestEntity.Status.OPEN)
                    .toList();
            for (BookRequestEntity r : openRequests) {
                r.close();
                requestDAO.update(r);
            }
        }
    }

    @Override
    @Transactional
    public void writeOffBook(int bookId) {
        BookEntity book = bookDAO.findById(bookId);
        if (book != null) inventory.writeOff(bookId);
    }

    @Override
    @Transactional
    public BookRequest requestBook(int bookId) {
        BookRequestEntity request = new BookRequestEntity(bookId, 1);
        BookRequestEntity saved = requestDAO.save(request);

        // Convert Entity to Model
        BookRequest.Status status = BookRequest.Status.valueOf(saved.getStatus().name());
        return new BookRequest(saved.getId(), saved.getBookId(), status, saved.getRequestCount(), saved.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookDetails(int bookId) {
        BookEntity entity = bookDAO.findById(bookId);
        if (entity == null) return null;

        // Convert Entity to Model
        Book.Status status = Book.Status.valueOf(entity.getStatus().name());
        Book model = new Book(entity.getId(), entity.getTitle(), status, entity.getPrice(), entity.getPublicationDate(), entity.getDescription());
        model.setArrivalDate(entity.getArrivalDate());
        return model;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBooksSortedByTitle() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus().name().equals(Book.Status.AVAILABLE.name()))
                .sorted(Comparator.comparing(BookEntity::getTitle))
                .map(this::convertToBookModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBooksSortedByPrice() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus().name().equals(Book.Status.AVAILABLE.name()))
                .sorted(Comparator.comparingDouble(BookEntity::getPrice))
                .map(this::convertToBookModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBooksSortedByDate() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus().name().equals(Book.Status.AVAILABLE.name()))
                .sorted(Comparator.comparing(BookEntity::getArrivalDate))
                .map(this::convertToBookModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBooksSortedByAvailability() {
        return bookDAO.findAll().stream()
                .filter(b -> b.getStatus().name().equals(Book.Status.AVAILABLE.name()))
                .sorted(Comparator.comparing(BookEntity::getStatus))
                .map(this::convertToBookModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getStaleBooks(int months) {
        return inventory.listStaleBooks(months); // вызов из инвентаря
    }

    // --- OrderService ---

    @Override
    @Transactional
    public Order createOrder(Map<Integer, Integer> items, String customerName) {
        OrderEntity order = new OrderEntity(customerName, items);
        OrderEntity saved = orderDAO.save(order);

        // Convert Entity to Model
        Order modelOrder = new Order(saved.getId(), saved.getItems(), saved.getCustomerName(), saved.getCreatedAt());
        modelOrder.setStatus(Order.Status.valueOf(saved.getStatus().name()));
        modelOrder.setTotalPrice(saved.getTotalPrice());
        if (saved.getClosedAt() != null) modelOrder.setClosedAt(saved.getClosedAt());

        // Проверяем отсутствие книг — создаём заявки
        items.keySet().forEach(bookId -> {
            Book book = inventory.getBook(bookId);
            if (book == null || book.getStatus() == Book.Status.ABSENT) {
                createRequestIfAbsent(bookId);
            }
        });

        return modelOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Order> getAllOrders() {
        List<OrderEntity> entities = orderDAO.findAll();
        Map<Integer, Order> map = new HashMap<>();
        for (OrderEntity e : entities) {
            Order model = new Order(e.getId(), e.getItems(), e.getCustomerName(), e.getCreatedAt());
            model.setStatus(Order.Status.valueOf(e.getStatus().name()));
            model.setTotalPrice(e.getTotalPrice());
            if (e.getClosedAt() != null) model.setClosedAt(e.getClosedAt());
            map.put(model.getId(), model);
        }
        return map;
    }

    @Override
    @Transactional
    public void cancelOrder(int orderId) {
        OrderEntity order = orderDAO.findById(orderId);
        if (order != null) {
            order.setStatus(OrderEntity.Status.CANCELLED);
            orderDAO.update(order);
        }
    }

    @Override
    @Transactional
    public void changeOrderStatus(int orderId, Order.Status status) {
        OrderEntity o = orderDAO.findById(orderId);
        if (o == null) return;

        if (status == Order.Status.COMPLETED) {
            boolean allAvailable = o.getItems().entrySet().stream().allMatch(e ->
                    inventory.getBook(e.getKey()) != null &&
                            inventory.getBook(e.getKey()).getStatus() == Book.Status.AVAILABLE &&
                            inventory.getQuantity(e.getKey()) >= e.getValue()
            );
            if (!allAvailable) throw new RuntimeException("Не все книги доступны для завершения заказа");

            o.getItems().forEach((id, qty) -> inventory.reduceStock(id, qty));
        }
        o.setStatus(OrderEntity.Status.valueOf(status.name()));
        orderDAO.update(o);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetails(int orderId) {
        OrderEntity entity = orderDAO.findById(orderId);
        if (entity == null) return null;

        // Convert Entity to Model
        Order model = new Order(entity.getId(), entity.getItems(), entity.getCustomerName(), entity.getCreatedAt());
        model.setStatus(Order.Status.valueOf(entity.getStatus().name()));
        model.setTotalPrice(entity.getTotalPrice());
        if (entity.getClosedAt() != null) model.setClosedAt(entity.getClosedAt());
        return model;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersSortedByDate() {
        return orderDAO.findAll().stream()
                .sorted(Comparator.comparing(OrderEntity::getCreatedAt))
                .map(this::convertToOrderModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersSortedByPrice() {
        return orderDAO.findAll().stream()
                .sorted(Comparator.comparingDouble(OrderEntity::getTotalPrice))
                .map(this::convertToOrderModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersSortedByStatus() {
        return orderDAO.findAll().stream()
                .sorted(Comparator.comparing(OrderEntity::getStatus))
                .map(this::convertToOrderModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getCompletedOrdersInPeriod(LocalDate from, LocalDate to) {
        return orderDAO.findAll().stream()
                .filter(o -> o.getStatus() == OrderEntity.Status.COMPLETED)
                .filter(o -> {
                    LocalDate date = o.getCreatedAt().toLocalDate();
                    return (date.isAfter(from) || date.isEqual(from)) &&
                            (date.isBefore(to) || date.isEqual(to));
                })
                .map(this::convertToOrderModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double getRevenueInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).stream()
                .peek(o -> o.recalculateTotal(inventory))
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public int getCompletedOrdersCountInPeriod(LocalDate from, LocalDate to) {
        return getCompletedOrdersInPeriod(from, to).size();
    }

    // --- RequestService ---

    @Override
    @Transactional(readOnly = true)
    public List<BookRequest> getRequestsSortedByTitle() {
        return requestDAO.findAll().stream()
                .sorted(Comparator.comparing(r -> {
                    BookEntity book = bookDAO.findById(r.getBookId());
                    return book != null ? book.getTitle() : "";
                }))
                .map(this::convertToBookRequestModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookRequest> getRequestsSortedByRequestCount() {
        return requestDAO.findAll().stream()
                .sorted(Comparator.comparingInt(BookRequestEntity::getRequestCount).reversed())
                .map(this::convertToBookRequestModel)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Integer, BookRequest> getAllRequests() {
        List<BookRequestEntity> entities = requestDAO.findAll();
        Map<Integer, BookRequest> map = new HashMap<>();
        for (BookRequestEntity e : entities) {
            BookRequest model = convertToBookRequestModel(e);
            map.put(model.getId(), model);
        }
        return map;
    }

    @Transactional
    private BookRequestEntity createRequestIfAbsent(int bookId) {
        BookRequestEntity request = new BookRequestEntity(bookId, 1);
        return requestDAO.save(request);
    }

    // --- Helper Methods ---

    private Book convertToBookModel(BookEntity entity) {
        Book.Status status = Book.Status.valueOf(entity.getStatus().name());
        Book model = new Book(entity.getId(), entity.getTitle(), status, entity.getPrice(), entity.getPublicationDate(), entity.getDescription());
        model.setArrivalDate(entity.getArrivalDate());
        return model;
    }

    private Order convertToOrderModel(OrderEntity entity) {
        Order model = new Order(entity.getId(), entity.getItems(), entity.getCustomerName(), entity.getCreatedAt());
        model.setStatus(Order.Status.valueOf(entity.getStatus().name()));
        model.setTotalPrice(entity.getTotalPrice());
        if (entity.getClosedAt() != null) model.setClosedAt(entity.getClosedAt());
        return model;
    }

    private BookRequest convertToBookRequestModel(BookRequestEntity entity) {
        BookRequest.Status status = BookRequest.Status.valueOf(entity.getStatus().name());
        return new BookRequest(entity.getId(), entity.getBookId(), status, entity.getRequestCount(), entity.getCreatedAt());
    }
}