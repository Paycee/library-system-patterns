package com.library.infrastructure.mongo;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.book.Genre;
import com.library.domain.book.Loan;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.*;

public class MongoBookRepository implements BookRepository {

    private final MongoCollection<Document> books;
    private static final Bson BORROWED =
            and(ne("latestLoan", null), eq("latestLoan.returnedAt", null));

    public MongoBookRepository(MongoDatabase database)
    {
        this.books = database.getCollection("books");
    }

    @Override
    public Optional<Book> findById(long id) {
        return Optional.ofNullable(books.find(eq("_id", id)).first()).map(this::toBook);
    }

    @Override
    public List<Book> findAll() {
        return toBooks(books.find());
    }

    @Override
    public List<Book> findAvailable() {
        return toBooks(books.find(or(eq("latestLoan", null),
                ne("latestLoan.returnedAt", null))));
    }

    @Override
    public List<Book> findBorrowed() {
        return toBooks(books.find(BORROWED));
    }

    @Override
    public List<Book> findBorrowedBy(long memberId) {
        return toBooks(books.find(and(BORROWED, eq("latestLoan.memberId", memberId))));
    }

    @Override
    public List<Book> findOverdue(LocalDate today) {
        return toBooks(books.find(and(BORROWED, lt("latestLoan.dueDate", today.toString()))));
    }

    @Override
    public void save(Book book) {
        books.replaceOne(eq("_id", book.getId()), toDocument(book), new ReplaceOptions().upsert(true));
    }

    @Override
    public List<Book> findPage(int page, int size) {
        return toBooks(books.find().sort(Sorts.ascending("_id")).skip(page * size).limit(size));
    }

    private Document toDocument(Book book) {
        return new Document("_id", book.getId())
                .append("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("pages", book.getPages())
                .append("genre", book.getGenre().name())
                .append("shelfId", book.getShelfId())
                .append("latestLoan", book.getLatestLoan().map(this::loanToDocument).orElse(null));
    }

    private Document loanToDocument(Loan loan) {
        return new Document("id", loan.id().toString())
                .append("memberId", loan.memberId())
                .append("borrowedAt", loan.borrowedAt().toString())
                .append("dueDate", loan.dueDate().toString())
                .append("returnedAt", loan.returnedAt() == null ? null : loan.returnedAt().toString());
    }

    private Book toBook(Document doc) {
        Document l = doc.get("latestLoan", Document.class);
        Loan loan = (l == null) ? null : new Loan(
                UUID.fromString(l.getString("id")),
                number(l, "memberId").longValue(),
                date(l, "borrowedAt"),
                date(l, "dueDate"),
                date(l, "returnedAt"));

        return Book.restore(
                number(doc, "_id").longValue(),
                doc.getString("title"),
                doc.getString("author"),
                number(doc, "pages").intValue(),
                Genre.valueOf(doc.getString("genre")),
                number(doc, "shelfId").longValue(),
                loan);
    }

    private List<Book> toBooks(FindIterable<Document> docs) {
        return docs.map(this::toBook).into(new ArrayList<>());
    }

    private static Number number(Document doc, String key) {
        return doc.get(key, Number.class);
    }

    private static LocalDate date(Document doc, String key) {
        String value = doc.getString(key);
        return value == null ? null : LocalDate.parse(value);
    }
}
