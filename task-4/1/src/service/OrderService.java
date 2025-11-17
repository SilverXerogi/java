package service;

import model.Order;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OrderService {

    Order createOrder(Map<String, Integer> items); // items: bookId -> qty
    void cancelOrder(String orderId);
    void changeOrderStatus(String orderId, Order.Status status);
    List<Order> listOrdersSortedByDate();
    List<Order> listOrdersSortedByPrice();
    List<Order> listOrdersSortedByStatus();
    List<Order> listCompletedOrdersInPeriod(Date from, Date to);
    double getRevenueInPeriod(Date from, Date to);
    int getCompletedOrdersCountInPeriod(Date from, Date to);
    Order getOrderDetails(String orderId);

}
