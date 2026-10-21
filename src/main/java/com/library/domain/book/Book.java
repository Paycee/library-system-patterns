package com.library.domain.book;

import com.library.domain.exception.BookNotAvailableException;
import com.library.domain.exception.BookNotBorrowedException;
import com.library.domain.exception.NotBorrowedByMemberException;

import java.time.LocalDate;
import java.util.Optional;

/**
 * KOREN AGREGATA "Knjiga".
 *
 * Agregat čine: Book + njena poslednja pozajmica (Loan).
 * U bazi su to dve tabele (books i loans), ali u kodu je to JEDNA celina,
 * i čuva se kroz JEDAN repository (BookRepository).
 *
 * Pravila koja knjiga čuva SAMA (za proveru je dovoljna sama knjiga):
 *  1. knjiga se ne može iznajmiti ako je već iznajmljena
 *  2. knjiga se ne može vratiti ako nije iznajmljena
 *  3. rok za vraćanje je LOAN_DAYS dana od iznajmljivanja
 *
 * Pravila koja NISU ovde (jer knjiga nema dovoljno informacija):
 *  - "član može imati najviše 3 knjige"  -> LibraryService
 *  - "član koji kasni ne može da iznajmi" -> LibraryService
 */
public class Book {
    public static final int LOAN_DAYS = 14;

    private final long id;
    private final String title;
    private final String author;
    private final int pages;
    private final Genre genre;
    private final long shelfId;          // knjiga zna NA KOJOJ je polici, ali polica nije njen deo
    private Loan latestLoan;             // null = nikad nije iznajmljivana

    /** Nova knjiga stiže u biblioteku: slobodna, bez istorije. */
    public Book(long id, String title, String author, int pages, Genre genre, long shelfId) {
        this(id, title, author, pages, genre, shelfId, null);
    }

    /** Koristi ga SAMO repository kad knjigu "oživljava" iz skladišta. */
    public static Book restore(long id, String title, String author, int pages,
                               Genre genre, long shelfId, Loan latestLoan) {
        return new Book(id, title, author, pages, genre, shelfId, latestLoan);
    }

    private Book(long id, String title, String author, int pages,
                 Genre genre, long shelfId, Loan latestLoan) {
        if (pages <= 0) {
            throw new IllegalArgumentException("Broj strana mora biti pozitivan");
        }
        this.id = id;
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.genre = genre;
        this.shelfId = shelfId;
        this.latestLoan = latestLoan;
    }


    private Book(Builder b) {
        this(b.id, b.title, b.author, b.pages, b.genre, b.shelfId, b.latestLoan);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder{
        private long id;
        private String title;
        private String author;
        private int pages;
        private Genre genre;
        private long shelfId;
        private Loan latestLoan;

        private Builder(){}

        public Builder id(long id) {this.id = id; return this;}
        public Builder title(String title) {this.title = title; return this;}
        public Builder author(String author) {this.author = author; return this;}
        public Builder pages(int pages) {this.pages = pages; return this;}
        public Builder genre(Genre genre) {this.genre = genre; return this;}
        public Builder shelfId(long shelfId) {this.shelfId = shelfId; return this;}
        public Builder latestLoan(Loan latestLoan) {this.latestLoan = latestLoan; return this;}

        public Book build()
        {
            if(id <= 0) throw new IllegalStateException("id je obavezan");
            if(title == null || title.isBlank()) throw new IllegalStateException("Naslov je obavezan");
            if(pages <= 0) throw new IllegalStateException("Broj strana mora biti pozitivan");
            if(genre == null) throw new IllegalStateException("Zanr je obavezan");
            if(shelfId <= 0) throw new IllegalStateException("Polica je obavezna");
            return new Book(this);
        }

    }
    // ===================== Poslovna pravila =====================

    public boolean isAvailable() {
        return latestLoan == null || latestLoan.isReturned();
    }

    public void borrow(long memberId, LocalDate today) {
        if(!isAvailable())
            throw new BookNotAvailableException("Knjiga "+ title +" nije dostupna");

        latestLoan = Loan.start(memberId, today, LOAN_DAYS);
    }

    public void returnBook(LocalDate today, long memberId) {
        if(isAvailable())
            throw  new BookNotBorrowedException("Knjiga je slobodna");

        if(!isBorrowedBy(memberId))
            throw  new NotBorrowedByMemberException("Ovaj clan nije iznajmio tu knjigu");
        latestLoan = latestLoan.markReturned(today);
    }

    public boolean isOverdue(LocalDate today) {
        return latestLoan != null && latestLoan.isOverdue(today);
    }

    public boolean isBorrowedBy(long memberId) {
        return !isAvailable() && latestLoan.memberId() == memberId;
    }

    // ===================== Samo čitanje, nema settera =====================

    public long getId()           { return id; }
    public String getTitle()      { return title; }
    public String getAuthor()     { return author; }
    public int getPages()         { return pages; }
    public Genre getGenre()       { return genre; }
    public long getShelfId()      { return shelfId; }
    public Optional<Loan> getLatestLoan() { return Optional.ofNullable(latestLoan); }

    @Override
    public String toString() {
        return "\"" + title + "\" (" + author + ")";
    }
}

