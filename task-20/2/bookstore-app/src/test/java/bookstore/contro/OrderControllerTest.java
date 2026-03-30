package bookstore.controller;

import bookstore.dto.OrderDTO;
import bookstore.model.BookStoreService;
import bookstore.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private BookStoreService bookStoreService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void getAllOrders_Positive_ReturnsOkWithOrders() {
        // Arrange
        Map<Integer, Order> mockOrders = Map.of(
                1, new Order(1, Map.of(1, 2), "Customer 1", java.time.LocalDateTime.now())
        );
        when(bookStoreService.getAllOrders()).thenReturn(mockOrders);

        // Act
        ResponseEntity<java.util.List<OrderDTO>> response = orderController.getAllOrders();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
        verify(bookStoreService).getAllOrders();
    }

    @Test
    void getOrderById_Positive_ReturnsOrder() {
        // Arrange
        int orderId = 1;
        Order mockOrder = new Order(orderId, Map.of(1, 2), "Customer", java.time.LocalDateTime.now());
        when(bookStoreService.getOrderDetails(orderId)).thenReturn(mockOrder);

        // Act
        ResponseEntity<OrderDTO> response = orderController.getOrderById(orderId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(bookStoreService).getOrderDetails(orderId);
    }

    @Test
    void getOrderById_Negative_OrderNotFound_ReturnsNotFound() {
        // Arrange
        int orderId = 999;
        when(bookStoreService.getOrderDetails(orderId)).thenReturn(null);

        // Act
        ResponseEntity<OrderDTO> response = orderController.getOrderById(orderId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        verify(bookStoreService).getOrderDetails(orderId);
    }

    @Test
    void deleteOrder_Positive_CallsServiceCancel() {
        // Arrange
        int orderId = 1;

        // Act
        ResponseEntity<Void> response = orderController.deleteOrder(orderId);

        // Assert
        assertEquals(204, response.getStatusCodeValue());
        verify(bookStoreService).cancelOrder(orderId);
    }
}