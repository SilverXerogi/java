package bookstore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class NotFoundController {

    // Обрабатывает все запросы, которые не найдены в других контроллерах
    @RequestMapping("/api/**")
    public ResponseEntity<String> handleNotFound(HttpServletRequest request) {
        String path = request.getRequestURI();
        String message = String.format(
                "Endpoint '%s' not found. Available endpoints: /api/books, /api/orders, /api/requests",
                path
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }
}