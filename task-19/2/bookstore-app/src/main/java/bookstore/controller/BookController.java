package bookstore.controller;

import bookstore.dto.BookDTO;
import bookstore.model.BookStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookStoreService bookStoreService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // ✅ Только USER и ADMIN
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<bookstore.model.Book> books = bookStoreService.getBooksSortedByTitle();

        List<BookDTO> dtos = books.stream()
                .map(this::convertToBookDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // ✅ Только USER и ADMIN
    public ResponseEntity<BookDTO> getBookById(@PathVariable int id) {
        bookstore.model.Book book = bookStoreService.getBookDetails(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToBookDTO(book));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // ✅ Только ADMIN
    public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO book) {
        // DTO -> Book (для вызова сервиса)
        bookstore.model.Book modelBook = convertFromBookDTO(book);

        // Вызов сервиса (например, добавление в инвентарь)
        // bookStoreService.addBookToInventory(modelBook.getId(), 1); // пример

        // Возврат созданного объекта как DTO
        return ResponseEntity.ok(book); // или обновлённый DTO
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ✅ Только ADMIN
    public ResponseEntity<BookDTO> updateBook(@PathVariable int id, @RequestBody BookDTO book) {
        // Обновление через сервис
        // bookStoreService.updateBook(id, convertFromBookDTO(book));

        return ResponseEntity.ok(book); // или обновлённый DTO
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ✅ Только ADMIN
    public ResponseEntity<Void> deleteBook(@PathVariable int id) {
        bookStoreService.writeOffBook(id); // пример вызова
        return ResponseEntity.noContent().build();
    }

    // --- Конвертеры ---

    private BookDTO convertToBookDTO(bookstore.model.Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setStatus(book.getStatus().name());
        dto.setPrice(book.getPrice());
        dto.setPublicationDate(book.getPublicationDate() != null ? book.getPublicationDate().toString() : null);
        dto.setArrivalDate(book.getArrivalDate() != null ? book.getArrivalDate().toString() : null);
        dto.setDescription(book.getDescription());
        return dto;
    }

    private bookstore.model.Book convertFromBookDTO(BookDTO dto) {
        bookstore.model.Book book = new bookstore.model.Book(
                dto.getId(),
                dto.getTitle(),
                bookstore.model.Book.Status.valueOf(dto.getStatus()),
                dto.getPrice(),
                dto.getPublicationDate() != null ? java.time.LocalDate.parse(dto.getPublicationDate()) : null,
                dto.getDescription()
        );
        if (dto.getArrivalDate() != null) {
            book.setArrivalDate(java.time.LocalDate.parse(dto.getArrivalDate()));
        }
        return book;
    }
}