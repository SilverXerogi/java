package model.service;

import model.BookRequest;

import java.util.List;

public interface RequestService {
    List<BookRequest> getRequestsSortedByTitle();
    List<BookRequest> getRequestsSortedByRequestCount();
}
