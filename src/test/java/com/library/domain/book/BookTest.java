package com.library.domain.book;

import com.library.domain.exception.BookNotAvailableException;
import com.library.domain.exception.BookNotBorrowedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test DOMENA: nema ni servisa ni repository-ja, samo knjiga i njena pravila.
 * Ovo je najbrža i najčistija vrsta testa.
 */
class BookTest {
    private static final LocalDate DAY_1 = LocalDate.of(2026, 10, 1);

    private Book newBook() {
        return new Book(1, "Na Drini ćuprija", "Ivo Andrić", 380, Genre.ROMAN, 1);
    }

    @Test
    void novaKnjigaJeSlobodna() {
        assertTrue(newBook().isAvailable());
    }

    @Test
    void iznajmljenaKnjigaNijeSlobodnaIRokJe14Dana() {
        Book book = newBook();
        book.borrow(7, DAY_1);

        assertFalse(book.isAvailable());
        assertTrue(book.isBorrowedBy(7));
        assertEquals(DAY_1.plusDays(14), book.getLatestLoan().orElseThrow().dueDate());
    }

    @Test
    void vecIznajmljenaKnjigaNeMozeDaSeIznajmi() {
        Book book = newBook();
        book.borrow(7, DAY_1);

        assertThrows(BookNotAvailableException.class, () -> book.borrow(8, DAY_1));
    }

    @Test
    void slobodnaKnjigaNeMozeDaSeVrati() {
        assertThrows(BookNotBorrowedException.class, () -> newBook().returnBook(DAY_1, 1));
    }

    @Test
    void kasniTekPosleRoka() {
        Book book = newBook();
        book.borrow(7, DAY_1);

        assertFalse(book.isOverdue(DAY_1.plusDays(14)));   // poslednji dan roka
        assertTrue(book.isOverdue(DAY_1.plusDays(15)));    // dan posle
    }

    @Test
    void vracenaKnjigaNeKasni() {
        Book book = newBook();
        book.borrow(7, DAY_1);
        book.returnBook(DAY_1.plusDays(20), 7);

        assertTrue(book.isAvailable());
        assertFalse(book.isOverdue(DAY_1.plusDays(30)));
    }
}
