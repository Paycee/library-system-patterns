package com.library.domain.event;

import java.time.LocalDate;

public interface LibraryEvent {

    public record BookBorrowed(long bookId, String title, long memberId, LocalDate dueDate)
            implements LibraryEvent{}

    public record BookReturned(long bookId, String title, long memberId, int fee)
            implements LibraryEvent{}
}
