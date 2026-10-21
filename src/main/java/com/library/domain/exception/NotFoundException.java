package com.library.domain.exception;

/** Traženi objekat (knjiga, član) ne postoji. */
public class NotFoundException extends LibraryException {
    public NotFoundException(String message) {
        super(message);
    }
}
