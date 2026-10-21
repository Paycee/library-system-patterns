package com.library.domain.search;

import com.library.domain.book.Book;

public record IsAvailable() implements BookExpression{
    @Override
    public boolean interpret(Book book) {
        return book.isAvailable();
    }
}
