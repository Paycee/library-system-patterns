package com.library.domain.exception;

/** Član kasni sa vraćanjem i ne može da iznajmi novu knjigu. */
public class MemberHasOverdueBooksException extends LibraryException {
    public MemberHasOverdueBooksException(String message) {
        super(message);
    }
}
