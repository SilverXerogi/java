package bookstore.model;

import bookstore.model.service.BookService;
import bookstore.model.service.OrderService;
import bookstore.model.service.RequestService;


public interface BookStoreService extends BookService, OrderService, RequestService {

}
