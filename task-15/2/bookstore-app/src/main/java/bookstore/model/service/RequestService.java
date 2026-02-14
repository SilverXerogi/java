package bookstore.model.service;

import bookstore.model.BookRequest;

import java.util.List;

public interface RequestService {
    List<BookRequest> getRequestsSortedByTitle();
    List<BookRequest> getRequestsSortedByRequestCount();
}
