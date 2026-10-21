package com.library.infrastructure.proxy;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.book.Genre;
import com.library.domain.exception.BookNotAvailableException;
import com.library.domain.fee.LateFeePolicy;
import com.library.infrastructure.memory.InMemoryBookRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimingProxyTest {

    @Test
    void proxyProsledjujePoziveIVracaIstiRezultat() {
        BookRepository books = TimingProxy.wrap(BookRepository.class, new InMemoryBookRepository());

        books.save(new Book(1, "Prokleta avlija", "Ivo Andrić", 150, Genre.ROMAN, 1));

        assertEquals("Prokleta avlija", books.findById(1).orElseThrow().getTitle());
        assertEquals(1, books.findAvailable().size());
    }

    @Test
    void proxyBacaOriginalniIzuzetak() {
        LateFeePolicy failing = daysLate -> { throw new BookNotAvailableException("test"); };
        LateFeePolicy proxied = TimingProxy.wrap(LateFeePolicy.class, failing);

        assertThrows(BookNotAvailableException.class, () -> proxied.calculate(5));
    }

    @Test
    void proxyNijeOriginalnaKlasa() {
        BookRepository proxied = TimingProxy.wrap(BookRepository.class, new InMemoryBookRepository());

        assertFalse(proxied instanceof InMemoryBookRepository);   // nova klasa, isti interfejs
        assertTrue(proxied instanceof BookRepository);
    }
}