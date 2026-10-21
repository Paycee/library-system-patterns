package com.library.domain.search;

import com.library.domain.book.Book;

public record TitleContains(String text) implements BookExpression{
    @Override
    public boolean interpret(Book book) {
        return book.getTitle().toLowerCase().contains(text.toLowerCase());
    }
}
