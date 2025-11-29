package model.io;

import model.Book;
import model.Inventory;

import java.io.*;
import java.time.LocalDate;

public class BookCSVHandler {

    private final Inventory inventory = Inventory.getInstance();

    /** Экспорт всех книг из инвентаря */
    public void exportBooks(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("id,title,status,price,arrivalDate");
            for (Book book : inventory.getCatalogMap().values()) {
                double price = inventory.getPrice(book.getId());
                LocalDate arrival = inventory.getArrivalDate(book.getId());
                writer.printf(
                        "%d,%s,%s,%.0f,%s%n",
                        book.getId(),
                        sanitize(book.getTitle()),
                        book.getStatus(),
                        price,
                        arrival != null ? arrival : ""
                );
            }
        }
    }

    /** Импорт книг из CSV. Обновляет или добавляет записи */
    public void importBooks(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine(); // пропускаем заголовок
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue;

                int id = Integer.parseInt(parts[0].trim());
                String title = parts[1].trim();
                Book.Status status = Book.Status.valueOf(parts[2].trim());
                double price = Double.parseDouble(parts[3].trim());
                LocalDate arrival = parts[4].isEmpty() ? LocalDate.now() : LocalDate.parse(parts[4].trim());

                Book existing = inventory.getBook(id);
                if (existing != null) {
                    existing.setStatus(status);
                } else {
                    Book newBook = new Book(id, title, status);
                    inventory.registerBook(newBook, 1, price);
                }
            }
        }
    }

    private String sanitize(String text) {
        return text == null ? "" : text.replace(",", " ").replace("\n", " ");
    }
}
