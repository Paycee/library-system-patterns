package com.library.domain.search;

import com.library.domain.book.Book;

public record PagesGreaterThan(int pages) implements BookExpression{
    @Override
    public boolean interpret(Book book) {
        return book.getPages() > pages;
    }
}
