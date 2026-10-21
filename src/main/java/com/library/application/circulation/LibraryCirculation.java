package com.library.application.circulation;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.event.LibraryEvent;
import com.library.domain.event.LibraryEventListener;
import com.library.domain.exception.LibraryException;
import com.library.domain.exception.NotFoundException;

public class LibraryCirculation implements CirculationMediator, LibraryEventListener {
    private static final long DEBT_WARNING = 1000;

    private final BookRepository books;
    private final WaitingList waitingList;
    private final Cashier cashier;
    private final Notifier notifier;

    public LibraryCirculation(BookRepository books, WaitingList waitingList,
                              Cashier cashier, Notifier notifier) {
        this.books = books;
        this.waitingList = waitingList;
        this.cashier = cashier;
        this.notifier = notifier;
        cashier.setMediator(this);            // blagajna zna samo za posrednika
    }

    @Override
    public void waitRequested(long memberId, long bookId) {
        Book book = books.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Knjiga ne postoji: " + bookId));

        if(book.isAvailable()){
            throw new LibraryException("Knjiga \"" + book.getTitle() + "\" je slobodna.");
        }

        if(book.isBorrowedBy(memberId)){
            throw new LibraryException("Knjiga \"" + book.getTitle() + "\" je već kod vas.");
        }

        int position = waitingList.join(bookId, memberId);
        notifier.send(memberId, "Upisani ste na listu za \"" + book.getTitle());
    }

    @Override
    public void debtChanged(long memberId, long totalDebt) {
        notifier.send(memberId, "Zaduzena je kazna. Ukupan dug: " + totalDebt + "din.");
        if(totalDebt > DEBT_WARNING){
            notifier.send(memberId, "Upozorenje: dug je veci od " + DEBT_WARNING + " din.");
        }
    }

    @Override
    public long debtOf(long memberId) {
        return cashier.debtOf(memberId);
    }

    @Override
    public void onEvent(LibraryEvent event) {
        if(event instanceof LibraryEvent.BookReturned returned){
            bookReturned(returned);
        }else if (event instanceof LibraryEvent.BookBorrowed borrowed){
            waitingList.remove(borrowed.bookId(), borrowed.memberId());
        }
    }

    private void bookReturned(LibraryEvent.BookReturned event) {
        if(event.fee() > 0){
            cashier.charge(event.memberId(), event.fee());
        }
        waitingList.pollNext(event.bookId()).ifPresent(next ->
                notifier.send(next, "Knjiga \"" + event.title() +"\" je ponovo slobodna"));
    }

}
