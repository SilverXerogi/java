package model.factory;

import model.BookRequest;

import java.time.LocalDateTime;

public class BookRequestFactory {

    public BookRequest createRequest(int id, int bookId, BookRequest.Status status, int requestCount, LocalDateTime createdAt) {
        return new BookRequest(id, bookId, status, requestCount, createdAt);
    }

    public BookRequest createNewRequest(int id, int bookId) {
        return new BookRequest(id, bookId, BookRequest.Status.OPEN, 1, LocalDateTime.now());
    }
}
