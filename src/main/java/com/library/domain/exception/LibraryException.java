package com.library.domain.exception;

/** Osnovni izuzetak za kršenje pravila biblioteke. */
public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }
}
