package bookstore.model.factory;


import bookstore.model.Order;

import java.time.LocalDateTime;
import java.util.Map;

public class OrderFactory {

    public Order createOrder(int id, Map<Integer, Integer> items, String customerName, LocalDateTime createdAt) {
        return new Order(id, items, customerName, createdAt);
    }

    public Order createNewOrder(int id, Map<Integer, Integer> items, String customerName) {
        return new Order(id, items, customerName, LocalDateTime.now());
    }
}
