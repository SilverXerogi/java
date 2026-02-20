package bookstore.model.io;



import bookstore.model.Inventory;
import bookstore.model.Order;



import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class OrderCSVHandler {

    private final Map<Integer, Order> orders;
    private final Inventory inventory;

    public OrderCSVHandler(Map<Integer, Order> orders, Inventory inventory) {
        this.orders = orders;
        this.inventory = inventory;
    }

    public void exportOrders(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("id,customerName,status,totalPrice,createdAt,items");
            for (Order order : orders.values()) {
                String itemsStr = mapToString(order.getItems());
                writer.printf(
                        "%d,%s,%s,%.2f,%s,%s%n",
                        order.getId(),
                        sanitize(order.getCustomerName()),
                        order.getStatus(),
                        order.getTotalPrice(),
                        order.getCreatedAt(),
                        itemsStr
                );
            }
        }
    }

    public void importOrders(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 6);
                if (parts.length < 6) continue;

                int id = Integer.parseInt(parts[0].trim());
                String customer = parts[1].trim();
                Order.Status status = Order.Status.valueOf(parts[2].trim());
                double total = Double.parseDouble(parts[3].trim());
                LocalDateTime created = LocalDateTime.parse(parts[4].trim());
                Map<Integer, Integer> items = parseItems(parts[5]);

                Order existing = orders.get(id);
                if (existing != null) {
                    existing.setStatus(status);
                    existing.recalculateTotal(inventory); // <-- передаём inventory
                } else {
                    Order newOrder = new Order(id, items, customer, created);
                    newOrder.setStatus(status);
                    newOrder.recalculateTotal(inventory); // <-- передаём inventory
                    orders.put(id, newOrder);
                }
            }
        }
    }

    private String mapToString(Map<Integer, Integer> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(e.getKey()).append(":").append(e.getValue());
        }
        return sb.toString();
    }

    private Map<Integer, Integer> parseItems(String str) {
        Map<Integer, Integer> map = new HashMap<>();
        if (str == null || str.isEmpty()) return map;

        String[] pairs = str.split(";");
        for (String p : pairs) {
            String[] kv = p.split(":");
            if (kv.length == 2) {
                try {
                    int bookId = Integer.parseInt(kv[0]);
                    int qty = Integer.parseInt(kv[1]);
                    map.put(bookId, qty);
                } catch (NumberFormatException ignored) { }
            }
        }
        return map;
    }

    private String sanitize(String text) {
        return text == null ? "" : text.replace(",", " ").replace("\n", " ");
    }
}