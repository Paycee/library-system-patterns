package com.library.application.search;

import com.library.domain.exception.LibraryException;

public class QuerySyntaxException extends LibraryException {
    public QuerySyntaxException(String message) {
        super(message);
    }
}
