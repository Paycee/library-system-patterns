package com.library.infrastructure.proxy;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.book.Genre;
import com.library.infrastructure.memory.InMemoryBookRepository;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LazyBookRepositoryTest {

    @Test
    void repositorySeNePraviDokNeZatreba() {
        AtomicInteger created = new AtomicInteger();
        new LazyBookRepository(() -> {
            created.incrementAndGet();
            return new InMemoryBookRepository();
        });

        assertEquals(0, created.get());   // proxy postoji, pravi repository ne
    }

    @Test
    void repositorySePraviSamoJednom() {
        AtomicInteger created = new AtomicInteger();
        BookRepository books = new LazyBookRepository(() -> {
            created.incrementAndGet();
            return new InMemoryBookRepository();
        });

        books.save(new Book(1, "Prokleta avlija", "Ivo Andrić", 150, Genre.ROMAN, 1));
        books.findAll();
        books.findAvailable();

        assertEquals(1, created.get());                       // tri poziva, jedno pravljenje
        assertTrue(books.findById(1).isPresent());            // i podaci su sačuvani
    }
}