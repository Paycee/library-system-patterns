package com.library.infrastructure.jdbc;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.book.Genre;
import com.library.domain.book.Loan;
import com.library.infrastructure.jdbc.dao.BookDao;
import com.library.infrastructure.jdbc.dao.BookRow;
import com.library.infrastructure.jdbc.dao.LoanDao;
import com.library.infrastructure.jdbc.dao.LoanRow;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * "BIBLIOTEKAR" koji radi sa pravom bazom.
 *
 * Koristi DVA magacionera (BookDao i LoanDao) i od njihovih sirovih redova
 * sklapa kompletan agregat Book (knjiga + poslednja pozajmica).
 * Servis nikad ne vidi BookRow ni LoanRow.
 */
public class JdbcBookRepository implements BookRepository {
    private final DataSource dataSource;   // treba samo za otvaranje transakcije u save()
    private final BookDao bookDao;
    private final LoanDao loanDao;

    public JdbcBookRepository(DataSource dataSource, BookDao bookDao, LoanDao loanDao) {
        this.dataSource = dataSource;
        this.bookDao = bookDao;
        this.loanDao = loanDao;
    }

    @Override
    public Optional<Book> findById(long id) {
        return bookDao.selectById(id).map(this::assemble);
    }

    @Override
    public List<Book> findAll() {
        return bookDao.selectAll().stream().map(this::assemble).toList();
    }

    @Override
    public List<Book> findAvailable() {
        Set<Long> borrowedIds = new HashSet<>(loanDao.selectBookIdsWithActiveLoan());

        return bookDao.selectAll().stream().filter(row -> !borrowedIds.contains(row.id()))
                .map(this::assemble).toList();
     }

    @Override
    public List<Book> findBorrowed() {
        return bookDao.selectByIds(loanDao.selectBookIdsWithActiveLoan()).stream().map(this::assemble).toList();
    }

    @Override
    public List<Book> findBorrowedBy(long memberId) {
        return bookDao.selectByIds(loanDao.selectBookIdsWithActiveLoanByMember(memberId)).stream().map(this::assemble).toList();
    }

    @Override
    public List<Book> findOverdue(LocalDate today) {
        List<Long> bookIds = new ArrayList<>(loanDao.selectBookIdsWithActiveLoanDueBefore(today));

        return bookDao.selectByIds(bookIds).stream().map(this::assemble).toList();
    }

    @Override
    public void save(Book book) {
        // Knjiga i njena pozajmica se upisuju u JEDNOJ transakciji (Unit of Work):
        // ne sme se desiti da se sačuva knjiga, a pozajmica ne.
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BookRow bookRow = new BookRow(
                        book.getId(), book.getTitle(), book.getAuthor(),
                        book.getPages(), book.getGenre().name(), book.getShelfId());

                if (bookDao.exists(conn, book.getId())) {
                    bookDao.update(conn, bookRow);
                } else {
                    bookDao.insert(conn, bookRow);
                }

                book.getLatestLoan().ifPresent(loan -> loanDao.upsert(conn, new LoanRow(
                        loan.id(), book.getId(), loan.memberId(),
                        loan.borrowedAt(), loan.dueDate(), loan.returnedAt())));

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Greška pri čuvanju knjige " + book.getId(), e);
        }
    }

    @Override
    public List<Book> findPage(int page, int size) {
        return bookDao.selectPage(size, page * size).stream().map(this::assemble).toList();
    }

    /** Sa dve police (books i loans) skuplja delove i sklapa celu knjigu. */
    private Book assemble(BookRow row) {
        Loan latestLoan = loanDao.selectLatestByBookId(row.id())
                .map(l -> new Loan(l.id(), l.memberId(), l.borrowedAt(), l.dueDate(), l.returnedAt()))
                .orElse(null);

        return Book.restore(
                row.id(), row.title(), row.author(), row.pages(),
                Genre.valueOf(row.genre()), row.shelfId(), latestLoan);
    }
}
