package com.library.domain.exception;

public class BookNotBorrowedException extends LibraryException {
    public BookNotBorrowedException(String message) {
        super(message);
    }
}
