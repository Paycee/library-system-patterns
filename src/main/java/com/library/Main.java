package com.library;

import com.library.application.LibraryService;
import com.library.domain.book.Book;
import com.library.domain.book.Genre;
import com.library.domain.event.EventPublisher;
import com.library.domain.exception.LibraryException;
import com.library.domain.fee.StandardFeePolicy;
import com.library.domain.member.Member;
import com.library.domain.shelf.Shelf;
import com.library.infrastructure.memory.InMemoryBookRepository;
import com.library.infrastructure.memory.InMemoryMemberRepository;
import com.library.infrastructure.memory.InMemoryShelfRepository;
import com.library.ui.ConsoleApp;

import java.time.Clock;

/**
 * Ovde se "sklapa" aplikacija: biramo implementacije i ubacujemo ih u servis.
 * U Springu ovaj posao radi Spring sam (IoC / Dependency Injection).
 *
 * Probaj: zameni InMemoryBookRepository sa JdbcBookRepository,
 * a LibraryService ostaje potpuno isti.
 */
public class Main {
    public static void main(String[] args) {
        try (var app = LibraryApp.inMemory()) {
            new ConsoleApp(app.service(), app.circulation()).run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
