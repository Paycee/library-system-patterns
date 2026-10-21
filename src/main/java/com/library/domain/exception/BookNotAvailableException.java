package com.library.domain.exception;

/** Knjiga je već iznajmljena. */
public class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String message) {
        super(message);
    }
}
