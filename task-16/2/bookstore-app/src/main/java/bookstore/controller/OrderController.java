package bookstore.controller;

import bookstore.dto.OrderDTO;
import bookstore.model.BookStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private BookStoreService bookStoreService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        Map<Integer, bookstore.model.Order> orders = bookStoreService.getAllOrders();

        List<OrderDTO> dtos = orders.values().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable int id) {
        bookstore.model.Order order = bookStoreService.getOrderDetails(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToOrderDTO(order));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO order) {
        // DTO -> Order (для вызова сервиса)
        bookstore.model.Order modelOrder = convertFromOrderDTO(order);

        return ResponseEntity.ok(order); // или обновлённый DTO
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable int id, @RequestBody OrderDTO order) {


        return ResponseEntity.ok(order); // или обновлённый DTO
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int id) {
        bookStoreService.cancelOrder(id); // пример вызова
        return ResponseEntity.noContent().build();
    }

    // --- Конвертеры ---

    private OrderDTO convertToOrderDTO(bookstore.model.Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomerName());
        dto.setStatus(order.getStatus().name());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setClosedAt(order.getClosedAt());
        dto.setItems(order.getItems());
        return dto;
    }

    private bookstore.model.Order convertFromOrderDTO(OrderDTO dto) {
        bookstore.model.Order order = new bookstore.model.Order(
                dto.getId(),
                dto.getItems(),
                dto.getCustomerName(),
                dto.getCreatedAt()
        );
        order.setStatus(bookstore.model.Order.Status.valueOf(dto.getStatus()));
        order.setTotalPrice(dto.getTotalPrice());
        order.setClosedAt(dto.getClosedAt());
        return order;
    }
}