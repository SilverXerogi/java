package bookstore.model.service;

import bookstore.model.Order;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order createOrder(Map<Integer, Integer> items, String customerName);
    Map<Integer, Order> getAllOrders();

    void cancelOrder(int orderId);
    void changeOrderStatus(int orderId, Order.Status status);
    Order getOrderDetails(int orderId);

    List<Order> getOrdersSortedByDate();
    List<Order> getOrdersSortedByPrice();
    List<Order> getOrdersSortedByStatus();
    List<Order> getCompletedOrdersInPeriod(LocalDate from, LocalDate to);

    double getRevenueInPeriod(LocalDate from, LocalDate to);
    int getCompletedOrdersCountInPeriod(LocalDate from, LocalDate to);
}