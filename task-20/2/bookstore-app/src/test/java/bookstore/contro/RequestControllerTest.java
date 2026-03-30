package bookstore.controller;

import bookstore.dto.RequestDTO;
import bookstore.model.BookStoreService;
import bookstore.model.BookRequest;
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
public class RequestControllerTest {

    @Mock
    private BookStoreService bookStoreService;

    @InjectMocks
    private RequestController requestController;

    @Test
    void getAllRequests_Positive_ReturnsOkWithRequests() {
        // Arrange
        Map<Integer, BookRequest> mockRequests = Map.of(
                1, new BookRequest(1, 1, BookRequest.Status.OPEN, 1, java.time.LocalDateTime.now())
        );
        when(bookStoreService.getAllRequests()).thenReturn(mockRequests);

        // Act
        ResponseEntity<java.util.List<RequestDTO>> response = requestController.getAllRequests();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
        verify(bookStoreService).getAllRequests();
    }

    @Test
    void getRequestById_Negative_NotImplemented_ReturnsNotFound() {
        // Act
        ResponseEntity<RequestDTO> response = requestController.getRequestById(1);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deleteRequest_Positive_DoesNothing() {
        // Arrange
        int requestId = 1;

        // Act
        ResponseEntity<Void> response = requestController.deleteRequest(requestId);

        // Assert
        assertEquals(204, response.getStatusCodeValue());
        // Удаление не реализовано, но должен вернуть 204
    }
}