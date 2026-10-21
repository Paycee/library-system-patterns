package com.library.domain.exception;

public class NotBorrowedByMemberException extends LibraryException {
    public NotBorrowedByMemberException(String message) {
        super(message);
    }
}
