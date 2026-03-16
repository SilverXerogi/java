package bookstore.model.service;

import bookstore.model.BookRequest;

import java.util.List;
import java.util.Map;

public interface RequestService {
    List<BookRequest> getRequestsSortedByTitle();
    List<BookRequest> getRequestsSortedByRequestCount();
    Map<Integer, BookRequest> getAllRequests();
}
