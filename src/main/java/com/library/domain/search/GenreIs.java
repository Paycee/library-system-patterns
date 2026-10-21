package com.library.domain.search;

import com.library.domain.book.Book;
import com.library.domain.book.Genre;

public record GenreIs(Genre genre) implements BookExpression{
    @Override
    public boolean interpret(Book book) {
        return book.getGenre() == genre;
    }
}
