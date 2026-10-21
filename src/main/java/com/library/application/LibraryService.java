package com.library.application;

import com.library.domain.book.Book;
import com.library.domain.book.BookCatalog;
import com.library.domain.book.BookRepository;
import com.library.domain.event.EventPublisher;
import com.library.domain.event.LibraryEvent;
import com.library.domain.exception.LoanLimitExceededException;
import com.library.domain.exception.MemberHasOverdueBooksException;
import com.library.domain.exception.NotFoundException;
import com.library.domain.fee.LateFeePolicy;
import com.library.domain.member.Member;
import com.library.domain.member.MemberRepository;
import com.library.domain.search.BookExpression;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SERVISNI SLOJ: koordinira operacije biblioteke.
 *
 * Servis radi tri stvari:
 *  1. učitava objekte preko repository-ja
 *  2. proverava pravila koja zahtevaju VIŠE objekata ili pretragu skladišta
 *  3. poziva metode domenskih objekata i čuva ih
 *
 * Servis NE proverava da li je knjiga slobodna: to zna sama knjiga (Book.borrow).
 *
 * Clock se ubacuje spolja da bi testovi mogli da "pomeraju vreme".
 * Da piše LocalDate.now() direktno, ne bi se moglo testirati kašnjenje.
 */
public class LibraryService {
    public static final int MAX_BOOKS_PER_MEMBER = 3;

    private final BookRepository books;
    private final MemberRepository members;
    private final Clock clock;
    private final LateFeePolicy feePolicy;
    private final EventPublisher event;

    public LibraryService(BookRepository books, MemberRepository members, Clock clock, LateFeePolicy feePolicy, EventPublisher event) {
        this.books = books;
        this.members = members;
        this.clock = clock;
        this.feePolicy = feePolicy;
        this.event = event;
    }

    // ===================== Akcije (tu su pravila) =====================

    public void borrowBook(long memberId, long bookId) {
        LocalDate today = LocalDate.now(clock);
        Member member = loadMember(memberId);
        Book book = loadBook(bookId);

        List<Book> memberBooks = books.findBorrowedBy(memberId);
        if(memberBooks.size() >= MAX_BOOKS_PER_MEMBER)
        {
            throw new LoanLimitExceededException("Iznajmili ste maksimalan broj knjiga");
        }

        boolean hasOverdue = memberBooks.stream().anyMatch(b -> b.isOverdue(today));
        if(hasOverdue)
        {
            throw new MemberHasOverdueBooksException("Niste vratili knjigu na vreme");
        }

        book.borrow(member.getId(), today);
        books.save(book);

        event.publish(new LibraryEvent.BookBorrowed(book.getId(), book.getTitle(), member.getId(), book.getLatestLoan().orElseThrow().dueDate()));
    }

    public long returnBook(long memberId, long bookId) {
        LocalDate today = LocalDate.now();
        Book book = loadBook(bookId);
        book.returnBook(LocalDate.now(clock), memberId);
        books.save(book);

        long daysLate = book.getLatestLoan().orElseThrow().daysOverdue(today);
        long fee = feePolicy.calculate(daysLate);

        event.publish(new LibraryEvent.BookReturned(book.getId(), book.getTitle(), memberId, (int)fee));

        return fee;
    }

    public List<Member> allMembers()
    {
        return members.findAll();
    }
    public Iterable<Book> catalog() { return new BookCatalog(books, 2); }

    public List<Book> search(BookExpression query){
        List<Book> result = new ArrayList<>();
        for(Book book : catalog()){
            if(query.interpret(book)){
                result.add(book);
            }
        }

        return result;
    }


    // ===================== Pregledi (samo čitanje) =====================

    public List<Book> availableBooks()            { return books.findAvailable(); }
    public List<Book> borrowedBooks()             { return books.findBorrowed(); }
    public List<Book> overdueBooks()              { return books.findOverdue(LocalDate.now(clock)); }
    public List<Book> booksOfMember(long memberId) { return books.findBorrowedBy(memberId); }

    // ===================== Pomoćne =====================

    private Book loadBook(long id) {
        return books.findById(id)
            .orElseThrow(() -> new NotFoundException("Knjiga ne postoji: " + id));
    }

    private Member loadMember(long id) {
        return members.findById(id)
            .orElseThrow(() -> new NotFoundException("Član ne postoji: " + id));
    }
}
