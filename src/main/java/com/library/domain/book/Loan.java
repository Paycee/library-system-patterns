package com.library.domain.book;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * DEO AGREGATA "Knjiga": jedna pozajmica.
 *
 * U bazi ima svoju tabelu (loans), ali NEMA svoj repository.
 * Pozajmica ne postoji sama za sebe, uvek ide uz knjigu (kao točak uz automobil).
 *
 * Record je nepromenljiv: kad se knjiga vrati, ne menjamo ovaj objekat,
 * nego pravimo novi sa popunjenim returnedAt.
 */
public record Loan(UUID id, long memberId, LocalDate borrowedAt, LocalDate dueDate, LocalDate returnedAt) {

    /** Nova pozajmica, počinje danas. */
    static Loan start(long memberId, LocalDate today, int loanDays) {
        return new Loan(UUID.randomUUID(), memberId, today, today.plusDays(loanDays), null);
    }

    public boolean isReturned() {
        return returnedAt != null;
    }

    public boolean isOverdue(LocalDate today) {
        return !isReturned() && today.isAfter(dueDate);
    }

    public long daysOverdue(LocalDate returneDate) {
        long days = ChronoUnit.DAYS.between(dueDate, returneDate);
        return Math.max(0, days);
    }

    /**
     * Nije public: samo Book (isti paket) sme da zatvori pozajmicu.
     * Servis ne može direktno da "vrati" pozajmicu, mora kroz knjigu.
     */
    Loan markReturned(LocalDate today) {
        return new Loan(id, memberId, borrowedAt, dueDate, today);
    }
}
