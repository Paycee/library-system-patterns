package com.library.application;

import com.library.domain.book.Book;
import com.library.domain.book.Genre;
import com.library.domain.event.EventPublisher;
import com.library.domain.exception.BookNotAvailableException;
import com.library.domain.exception.LoanLimitExceededException;
import com.library.domain.exception.MemberHasOverdueBooksException;
import com.library.domain.exception.NotFoundException;
import com.library.domain.fee.NoFeePolicy;
import com.library.domain.fee.StandardFeePolicy;
import com.library.domain.member.Member;
import com.library.infrastructure.memory.InMemoryBookRepository;
import com.library.infrastructure.memory.InMemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test SERVISA: koristi in-memory repository-je, pa ne treba baza.
 * Clock je fiksiran, pa možemo da "pomeramo vreme" i testiramo kašnjenje.
 */
class LibraryServiceTest {
    private static final LocalDate DAY_1 = LocalDate.of(2026, 10, 1);
    private static final long PERA = 1, MIKA = 2;

    private InMemoryBookRepository books;
    private InMemoryMemberRepository members;

    @BeforeEach
    void setUp() {
        books = new InMemoryBookRepository();
        members = new InMemoryMemberRepository();
        members.save(new Member(PERA, "Pera"));
        members.save(new Member(MIKA, "Mika"));
        for (long id = 1; id <= 5; id++) {
            books.save(new Book(id, "Knjiga " + id, "Autor", 100, Genre.ROMAN, 1));
        }
    }

    /** Servis kome je "danas" zadati datum. */
    private LibraryService serviceOn(LocalDate day) {
        Clock clock = Clock.fixed(day.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        return new LibraryService(books, members, clock, new NoFeePolicy(), new EventPublisher());
    }

    @Test
    void clanIznajmljujeSlobodnuKnjigu() {
        var library = serviceOn(DAY_1);
        library.borrowBook(PERA, 1);

        assertEquals(1, library.booksOfMember(PERA).size());
        assertEquals(4, library.availableBooks().size());
        assertEquals(1, library.borrowedBooks().size());
    }

    @Test
    void dvaClanaNeMoguDaUzmuIstuKnjigu() {
        var library = serviceOn(DAY_1);
        library.borrowBook(PERA, 1);

        assertThrows(BookNotAvailableException.class, () -> library.borrowBook(MIKA, 1));
    }

    @Test
    void clanNeMozeImatiViseOdTriKnjige() {
        var library = serviceOn(DAY_1);
        library.borrowBook(PERA, 1);
        library.borrowBook(PERA, 2);
        library.borrowBook(PERA, 3);

        assertThrows(LoanLimitExceededException.class, () -> library.borrowBook(PERA, 4));
    }

    @Test
    void clanKojiKasniNeMozeDaIznajmiNovu() {
        serviceOn(DAY_1).borrowBook(PERA, 1);

        var library20DanaKasnije = serviceOn(DAY_1.plusDays(20));
        assertThrows(MemberHasOverdueBooksException.class,
                     () -> library20DanaKasnije.borrowBook(PERA, 2));
    }

    @Test
    void posleVracanjaKnjigaJeOpetSlobodna() {
        var library = serviceOn(DAY_1);
        library.borrowBook(PERA, 1);
        library.returnBook(1,1);

        assertEquals(5, library.availableBooks().size());
        assertTrue(library.booksOfMember(PERA).isEmpty());
    }

    @Test
    void pregledKasnjenja() {
        serviceOn(DAY_1).borrowBook(PERA, 1);
        serviceOn(DAY_1).borrowBook(MIKA, 2);
        serviceOn(DAY_1.plusDays(10)).borrowBook(MIKA, 3);

        var library = serviceOn(DAY_1.plusDays(20));
        assertEquals(2, library.overdueBooks().size());   // knjige 1 i 2, ne i 3
    }

    @Test
    void nepostojeciClanIliKnjiga() {
        var library = serviceOn(DAY_1);
        assertThrows(NotFoundException.class, () -> library.borrowBook(99, 1));
        assertThrows(NotFoundException.class, () -> library.borrowBook(PERA, 99));
    }
}
