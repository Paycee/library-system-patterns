package com.library.domain.search;

import com.library.domain.book.Book;

public interface BookExpression {
    boolean interpret(Book book);
}
