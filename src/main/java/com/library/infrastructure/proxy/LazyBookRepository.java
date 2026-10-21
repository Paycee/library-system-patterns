package com.library.infrastructure.proxy;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class LazyBookRepository implements BookRepository {

    private final Supplier<BookRepository> factory;
    private BookRepository real;

    public LazyBookRepository(Supplier<BookRepository> factory) {
        this.factory = factory;
    }

    private synchronized BookRepository real() {
        if(real == null)
        {
            System.out.println("    [lazy] prvi poziv, pravim prvi repository...");
            real = factory.get();
        }

        return real;
    }


    @Override
    public Optional<Book> findById(long id) {
        return real().findById(id);
    }

    @Override
    public List<Book> findAll() {
        return real().findAll();
    }

    @Override
    public List<Book> findAvailable() {
        return real().findAvailable();
    }

    @Override
    public List<Book> findBorrowed() {
        return real().findBorrowed();
    }

    @Override
    public List<Book> findBorrowedBy(long memberId) {
        return real().findBorrowedBy(memberId);
    }

    @Override
    public List<Book> findOverdue(LocalDate today) {
        return real().findOverdue(today);
    }

    @Override
    public List<Book> findPage(int page, int size) { return real().findPage(page, size); }

    @Override
    public void save(Book book) {
        real().save(book);
    }
}
