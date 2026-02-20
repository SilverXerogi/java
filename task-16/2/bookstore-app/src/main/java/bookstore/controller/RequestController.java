package bookstore.controller;

import bookstore.dto.RequestDTO;
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
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private BookStoreService bookStoreService;

    @GetMapping
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        Map<Integer, bookstore.model.BookRequest> requests = bookStoreService.getAllRequests();

        List<RequestDTO> dtos = requests.values().stream()
                .map(this::convertToRequestDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable int id) {

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<RequestDTO> createRequest(@RequestBody RequestDTO request) {

        bookstore.model.BookRequest modelRequest = convertFromRequestDTO(request);

        return ResponseEntity.ok(request); // или обновлённый DTO
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestDTO> updateRequest(@PathVariable int id, @RequestBody RequestDTO request) {


        return ResponseEntity.ok(request); // или обновлённый DTO
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable int id) {

        return ResponseEntity.noContent().build();
    }

    // --- Конвертеры ---

    private RequestDTO convertToRequestDTO(bookstore.model.BookRequest request) {
        RequestDTO dto = new RequestDTO();
        dto.setId(request.getId());
        dto.setBookId(request.getBookId());
        dto.setStatus(request.getStatus().name());
        dto.setRequestCount(request.getRequestCount());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setClosedAt(request.getClosedAt());
        return dto;
    }

    private bookstore.model.BookRequest convertFromRequestDTO(RequestDTO dto) {
        bookstore.model.BookRequest request = new bookstore.model.BookRequest(
                dto.getId(),
                dto.getBookId(),
                bookstore.model.BookRequest.Status.valueOf(dto.getStatus()),
                dto.getRequestCount(),
                dto.getCreatedAt()
        );
        request.setClosedAt(dto.getClosedAt());
        return request;
    }
}