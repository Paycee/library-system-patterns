package com.library.infrastructure.memory;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * Implementacija "bez baze": knjige su u običnoj mapi.
 * Koristi se u testovima i za učenje. Servis ne primećuje razliku u odnosu na JDBC verziju.
 *
 * Napomena: ovde čuvamo iste objekte koje dobijemo. Prava baza bi pri svakom
 * findById pravila nov objekat, pa bi zaboravljen save() značio izgubljenu promenu.
 * Ovde se to ne bi primetilo, i to je jedna od mana in-memory testova.
 */
public class InMemoryBookRepository implements BookRepository {
    private final Map<Long, Book> storage = new LinkedHashMap<>();

    @Override public Optional<Book> findById(long id) { return Optional.ofNullable(storage.get(id)); }
    @Override public List<Book> findAll()             { return List.copyOf(storage.values()); }
    @Override public void save(Book book)             { storage.put(book.getId(), book); }

    @Override
    public List<Book> findAvailable() {
        return storage.values().stream().filter(Book::isAvailable).toList();
    }

    @Override
    public List<Book> findBorrowed() {
        return storage.values().stream().filter(b -> !b.isAvailable()).toList();
    }

    @Override
    public List<Book> findBorrowedBy(long memberId) {
        return storage.values().stream().filter(b -> b.isBorrowedBy(memberId)).toList();
    }

    @Override
    public List<Book> findOverdue(LocalDate today) {
        return storage.values().stream().filter(b -> b.isOverdue(today)).toList();
    }

    @Override
    public List<Book> findPage(int page, int size) {
        return storage.values().stream()
                .sorted(Comparator.comparingLong(Book::getId))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }
}
