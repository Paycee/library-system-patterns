package com.library.infrastructure.jdbc.dao;

/** Sirov red iz tabele books. Nema pravila, samo kolone. */
public record BookRow(long id, String title, String author, int pages, String genre, long shelfId) {}
