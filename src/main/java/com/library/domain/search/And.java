package com.library.domain.search;

import com.library.domain.book.Book;

public record And(BookExpression left, BookExpression right) implements BookExpression{
    @Override
    public boolean interpret(Book book) {
        return left.interpret(book) && right.interpret(book);
    }
}
