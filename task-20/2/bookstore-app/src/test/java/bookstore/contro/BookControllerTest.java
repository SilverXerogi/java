package bookstore.controller;

import bookstore.dto.BookDTO;
import bookstore.model.Book;
import bookstore.model.BookStoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    @Mock
    private BookStoreService bookStoreService;

    @InjectMocks
    private BookController bookController;

    @Test
    void getAllBooks_Positive_ReturnsOkWithBooks() {
        // Arrange
        Book book1 = new Book(1, "Book 1", Book.Status.AVAILABLE, 100.0, java.time.LocalDate.now(), "Desc");
        Book book2 = new Book(2, "Book 2", Book.Status.ABSENT, 200.0, java.time.LocalDate.now(), "Desc2");
        when(bookStoreService.getBooksSortedByTitle()).thenReturn(Arrays.asList(book1, book2));

        // Act
        ResponseEntity<List<BookDTO>> response = bookController.getAllBooks();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(bookStoreService).getBooksSortedByTitle();
    }

    @Test
    void getAllBooks_Positive_NoBooks_ReturnsEmptyList() {
        // Arrange
        when(bookStoreService.getBooksSortedByTitle()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<BookDTO>> response = bookController.getAllBooks();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(bookStoreService).getBooksSortedByTitle();
    }

    @Test
    void getBookById_Positive_ReturnsBook() {
        // Arrange
        int bookId = 1;
        Book mockBook = new Book(bookId, "Test Book", Book.Status.AVAILABLE, 100.0, java.time.LocalDate.now(), "Desc");
        when(bookStoreService.getBookDetails(bookId)).thenReturn(mockBook);

        // Act
        ResponseEntity<BookDTO> response = bookController.getBookById(bookId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Test Book", response.getBody().getTitle());
        verify(bookStoreService).getBookDetails(bookId);
    }

    @Test
    void getBookById_Negative_BookNotFound_ReturnsNotFound() {
        // Arrange
        int bookId = 999;
        when(bookStoreService.getBookDetails(bookId)).thenReturn(null);

        // Act
        ResponseEntity<BookDTO> response = bookController.getBookById(bookId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        verify(bookStoreService).getBookDetails(bookId);
    }

    @Test
    void deleteBook_Positive_CallsServiceWriteOff() {
        // Arrange
        int bookId = 1;

        // Act
        ResponseEntity<Void> response = bookController.deleteBook(bookId);

        // Assert
        assertEquals(204, response.getStatusCodeValue());
        verify(bookStoreService).writeOffBook(bookId);
    }
}