package com.library.domain.search;

import com.library.domain.book.Book;

public record AuthorContains(String text) implements BookExpression{
    @Override
    public boolean interpret(Book book) {
        return book.getAuthor().toLowerCase().contains(text.toLowerCase());
    }
}
