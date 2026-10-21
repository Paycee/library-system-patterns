package com.library.domain.member;

/**
 * KOREN AGREGATA "Član".
 *
 * Član za sada nema svoja pravila. Primeti: pravilo "najviše 3 knjige"
 * NIJE ovde, jer član ne zna koje knjige drži. To zna BookRepository,
 * pa to pravilo proverava LibraryService.
 */
public class Member {
    private final long id;
    private final String name;

    public Member(long id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Član mora imati ime");
        }
        this.id = id;
        this.name = name;
    }

    public long getId()     { return id; }
    public String getName() { return name; }
}
