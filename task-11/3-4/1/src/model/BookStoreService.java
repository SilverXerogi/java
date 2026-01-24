package model;

import model.service.BookService;
import model.service.OrderService;
import model.service.RequestService;

import java.util.List;

public interface BookStoreService extends BookService, OrderService, RequestService {

}
