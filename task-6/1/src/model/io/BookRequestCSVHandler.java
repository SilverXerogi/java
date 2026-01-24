package model.io;

import model.BookRequest;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Map;

public class BookRequestCSVHandler {

    private final Map<Integer, BookRequest> requests;

    public BookRequestCSVHandler(Map<Integer, BookRequest> requests) {
        this.requests = requests;
    }

    /** Экспорт всех запросов в CSV */
    public void exportRequests(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("id,bookId,status,requestCount,createdAt,closedAt");
            for (BookRequest req : requests.values()) {
                writer.printf(
                        "%d,%d,%s,%d,%s,%s%n",
                        req.getId(),
                        req.getBookId(),
                        req.getStatus(),
                        req.getRequestCount(),
                        req.getCreatedAt(),
                        req.getClosedAt() != null ? req.getClosedAt() : ""
                );
            }
        }
    }

    /** Импорт CSV с обновлением статусов и добавлением новых */
    public void importRequests(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;

                int id = Integer.parseInt(parts[0].trim());
                int bookId = Integer.parseInt(parts[1].trim());
                BookRequest.Status status = BookRequest.Status.valueOf(parts[2].trim());
                int count = Integer.parseInt(parts[3].trim());
                LocalDateTime created = LocalDateTime.parse(parts[4].trim());
                LocalDateTime closed = parts[5].isEmpty() ? null : LocalDateTime.parse(parts[5].trim());

                BookRequest existing = requests.get(id);
                if (existing != null) {
                    existing.setStatus(status);
                } else {
                    BookRequest newReq = new BookRequest(id, bookId, status, count, created);
                    if (closed != null) newReq.close();
                    requests.put(id, newReq);
                }
            }
        }
    }
}
