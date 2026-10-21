package com.library.domain.search;

import com.library.domain.book.Book;

public record Not(BookExpression inner) implements BookExpression{
    @Override
    public boolean interpret(Book book) {
        return !inner.interpret(book);
    }
}
