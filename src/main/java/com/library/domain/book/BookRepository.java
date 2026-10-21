package com.library.domain.book;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * "BIBLIOTEKAR" za knjige.
 *
 * Interfejs je u DOMENSKOM sloju i govori jezikom biblioteke.
 * Nigde ne pominje tabele, SQL ni JDBC: poslovni kod ne zna gde su knjige.
 *
 * Implementacije su u infrastrukturi:
 *  - InMemoryBookRepository (za testove i učenje)
 *  - JdbcBookRepository     (prava baza, koristi DAO-ove)
 */
public interface BookRepository {
    Optional<Book> findById(long id);
    List<Book> findAll();
    List<Book> findAvailable();
    List<Book> findBorrowed();
    List<Book> findBorrowedBy(long memberId);
    List<Book> findOverdue(LocalDate today);
    List<Book> findPage(int page, int size);
    void save(Book book);
}
