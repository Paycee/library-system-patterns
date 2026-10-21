package com.library.ui;

import com.library.application.LibraryService;
import com.library.application.circulation.CirculationMediator;
import com.library.application.search.BookQueryParser;
import com.library.domain.book.Book;
import com.library.domain.exception.LibraryException;
import com.library.domain.member.Member;
import com.library.domain.search.BookExpression;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
    private final LibraryService library;
    private final CirculationMediator circulation;
    private final Scanner in = new Scanner(System.in);
    private Member current;

    public ConsoleApp(LibraryService library, CirculationMediator circulation)
    {
        this.library = library;
        this.circulation = circulation;
    }

    public void run() {
        chooseMember();
        while (true) {
            System.out.println("\n--- " + current.getName() + " ---");
            System.out.println("1) Slobodne  2) Moje knjige  3) Iznajmi  4) Vrati  5) Promeni člana 6) Katalog 7) Pretraga 8) Rezervisi 9) Dug 0) Izlaz");

            try {
                switch (in.nextLine().trim()) {
                    case "1" -> printBooks(library.availableBooks());
                    case "2" -> printBooks(library.booksOfMember(current.getId()));
                    case "3" -> {
                        printBooks(library.availableBooks());
                        library.borrowBook(current.getId(), readId("Id knjige: "));
                        System.out.println("Iznajmljeno.");
                    }
                    case "4" -> {
                        List<Book> myBooks = library.booksOfMember(current.getId());
                        printBooks(myBooks);
                        if (!myBooks.isEmpty()) {
                            long fee = library.returnBook(current.getId(), readId("Id knjige: "));
                            System.out.println("Vraćeno.\n" + "Kazna je "+ fee + " din.");
                        }
                    }
                    case "5" -> chooseMember();
                    case "6" -> {
                        for(Book b : library.catalog()){
                            System.out.println(b.getId() + ") " + b);
                        }
                    }
                    case "7" ->{
                        System.out.println("Primeri: author:Andrić | genre:KRIMI AND pages<250 | available AND NOT author:Kristi");
                        System.out.print("Upit: ");
                        BookExpression query = BookQueryParser.parse(in.nextLine());
                        System.out.println("Stablo: " + query);
                        printBooks(library.search(query));
                    }
                    case "8" -> {
                        printBooks(library.borrowedBooks());
                        circulation.waitRequested(current.getId(), readId("Id knjige: "));
                    }
                    case "9" -> System.out.println("Dug: " + circulation.debtOf(current.getId()) + " din.");
                    case "0" -> { return; }
                    default -> System.out.println("Nepoznata opcija.");
                }
            } catch (LibraryException e) {
                System.out.println("Greška: " + e.getMessage());
            }
        }
    }

    private void chooseMember() {
        List<Member> members = library.allMembers();
        members.forEach(m -> System.out.println(m.getId() + ") " + m.getName()));

        while (true) {
            long id = readId("Izaberi člana: ");
            Optional<Member> found = members.stream().filter(m -> m.getId() == id).findFirst();
            if (found.isPresent()) {
                current = found.get();
                return;
            }
            System.out.println("Nema člana sa tim brojem.");
        }
    }

    private long readId(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Long.parseLong(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Unesi broj.");
            }
        }
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("(nema knjiga)");
        }
        books.forEach(b -> System.out.println(b.getId() + ") " + b));
    }
}