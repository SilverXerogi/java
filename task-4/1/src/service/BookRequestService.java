package service;

import model.BookRequest;

import java.util.List;

public interface BookRequestService {
    List<BookRequest> listRequestsSortedByTitle();
    List<BookRequest> listRequestsSortedByRequestCount();
    BookRequest requestBook(String bookId);
}
