# Library – Design Patterns in Practice

![Java](https://img.shields.io/badge/Java-17-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.4-blue)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![JUnit](https://img.shields.io/badge/tests-JUnit%205-success)

A console application for borrowing books, built to apply **fifteen design patterns** and the five SOLID principles to a small, easy-to-follow domain. The library itself is just the setting: every pattern in the project solves a concrete problem that came up during development, and each one is documented below using the same structure.

Spring is intentionally **not** used. Everything a framework would normally do for you (wiring objects, transactions, proxies, events) is written by hand, so you can see how it actually works.

> **Note:** the console UI and exception messages are in Serbian. Code, class names and this documentation are in English.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Getting Started](#2-getting-started)
3. [Structure and Dependencies](#3-structure-and-dependencies)
4. [Flows Through the Application](#4-flows-through-the-application)
5. [Design Patterns](#5-design-patterns)
    - [Architectural: Repository, DAO, Unit of Work](#51-architectural-patterns)
    - [Creational: Singleton, Factory Method, Builder](#52-creational-patterns)
    - [Structural: Adapter, Decorator, Proxy, Facade, Composite](#53-structural-patterns)
    - [Behavioral: Strategy, Observer, Mediator, Iterator, Interpreter](#54-behavioral-patterns)
6. [SOLID Principles in the Project](#6-solid-principles-in-the-project)
7. [Tests](#7-tests)
8. [Limitations and Future Work](#8-limitations-and-future-work)

---

## 1. Overview

The user picks a library member and then chooses from the menu:

| Option | What it does |
|---|---|
| 1 Available | list available books |
| 2 My books | books the member currently holds |
| 3 Borrow | borrow an available book |
| 4 Return | return a book and get the late fee, if any |
| 5 Switch member | pick another member |
| 6 Catalog | all books, loaded page by page |
| 7 Search | query such as `author:Andrić AND pages>200` |
| 8 Wait for book | join the waiting list for a borrowed book |
| 9 My debt | member's total debt from late fees |

Business rules:

| Rule | Enforced in |
|---|---|
| A borrowed book cannot be borrowed again | `Book.borrow` |
| An available book cannot be returned | `Book.returnBook` |
| Only the member who borrowed a book can return it | `Book.returnBook` |
| The loan period is 14 days | `Book` / `Loan` |
| A member can hold at most 3 books | `LibraryService.borrowBook` |
| A member with an overdue book cannot borrow another | `LibraryService.borrowBook` |
| The late fee is 30 RSD per day, at most 500 RSD per return | `StandardFeePolicy` + `MaxFeeDecorator` |
| Only borrowed books can have a waiting list | `LibraryCirculation` |
| A member is warned when their debt exceeds 1000 RSD | `LibraryCirculation` |

Breaking a rule throws an exception that extends `LibraryException`. The console catches it, prints the message and keeps running.

**Tech stack:** Java 17, Maven, MySQL 8.4, MongoDB 7.0, Docker Compose, JUnit 5, plain JDBC (no ORM), MongoDB Java Sync Driver.

---

## 2. Getting Started

### Prerequisites

- JDK 17 or newer
- Maven 3.9+
- Docker Desktop (for the MySQL and MongoDB variants)

### Run the databases

```bash
git clone <repository-url>
cd biblioteka

docker compose up -d                              # MySQL on port 3307
docker compose -f docker-compose.mongo.yml up -d  # MongoDB on port 27018
```

On first start, MySQL runs `schema.sql` and `data.sql`, and MongoDB runs `mongo-init.js`. Data is kept in Docker volumes, so it survives restarts. To start from scratch, remove the volumes (`docker compose down -v`).

### Configuration

Settings live in `src/main/resources/library.properties`:

```properties
mysql.url=jdbc:mysql://localhost:3307/biblioteka
mysql.user=biblioteka
mysql.password=biblioteka
mongo.uri=mongodb://root:root@localhost:27018/?authSource=admin
fee.max=500
```

These are local development credentials only.

### Run the application

There are three entry points. Run them from your IDE, or with Maven:

```bash
mvn compile exec:java -Dexec.mainClass=com.library.Main        # in memory, no Docker needed
mvn compile exec:java -Dexec.mainClass=com.library.MainMySql   # MySQL
mvn compile exec:java -Dexec.mainClass=com.library.MainMongo   # MongoDB (books) + MySQL (members)
```

| Entry point | Books | Members |
|---|---|---|
| `Main` | in memory | in memory |
| `MainMySql` | MySQL | MySQL |
| `MainMongo` | MongoDB | MySQL |

### Run the tests

```bash
mvn test
```

All tests run without a database.

---

## 3. Structure and Dependencies

### 3.1 Layers

```
┌───────────────────────────────────────────────────────────┐
│ ui              ConsoleApp                                │  input and output
├───────────────────────────────────────────────────────────┤
│ application     LibraryService                            │  coordination
│                 search/       BookQueryParser             │
│                 circulation/  LibraryCirculation, ...     │
├───────────────────────────────────────────────────────────┤
│ domain          Book, Loan, Member, Shelf                 │  concepts and rules
│                 BookRepository, MemberRepository          │  (interfaces)
│                 LateFeePolicy, BookExpression, events     │
├───────────────────────────────────────────────────────────┤
│ infrastructure  memory / jdbc / mongo / proxy /           │  technical details
│                 notification                              │  (implements domain)
└───────────────────────────────────────────────────────────┘
        LibraryApp + Main classes: wiring (composition root)
```

**Dependency rule:** each layer only knows about the layers below it, except infrastructure, which depends on the domain because it implements the domain's interfaces. The domain knows nothing about the database, the console or the service.

### 3.2 Packages

```
com.library
├── domain
│   ├── book          Book, Loan, Genre, BookRepository, BookCatalog
│   ├── member        Member, MemberRepository
│   ├── shelf         Shelf, ShelfRepository
│   ├── fee           LateFeePolicy, StandardFeePolicy, StudentFeePolicy,
│   │                 NoFeePolicy, MaxFeeDecorator
│   ├── search        BookExpression, AuthorContains, TitleContains, GenreIs,
│   │                 PagesGreaterThan, PagesLessThan, IsAvailable, And, Or, Not
│   ├── event         LibraryEvent, LibraryEventListener, EventPublisher,
│   │                 ActivityLogListener, StatisticsListener
│   └── exception     LibraryException and subclasses
├── application
│   ├── LibraryService
│   ├── search        BookQueryParser, QuerySyntaxException
│   └── circulation   CirculationMediator, LibraryCirculation,
│                     WaitingList, Cashier, Notifier
├── infrastructure
│   ├── memory        InMemoryBookRepository, InMemoryMemberRepository, ...
│   ├── jdbc          JdbcBookRepository, JdbcMemberRepository,
│   │   │             SimpleDataSource, DataAccessException
│   │   └── dao       BookDao, LoanDao, MemberDao, ShelfDao + *Row classes
│   ├── mongo         MongoBookRepository
│   ├── proxy         TimingProxy, LazyBookRepository
│   └── notification  ConsoleNotifier
├── config            LibraryConfig
├── ui                ConsoleApp
├── LibraryApp
└── Main, MainMySql, MainMongo
```

The top-level package follows the **layer**, and the sub-package name follows the **feature** or the **technology**. That is why search has two packages: the expressions (`domain/search`) define what a condition means, while the parser (`application/search`) translates text into expressions. The same goes for repositories: the interface is in the domain, the implementations are in infrastructure.

### 3.3 Who Depends on What

| Class | Depends on | Does not know about |
|---|---|---|
| `ConsoleApp` | `LibraryService`, `CirculationMediator`, `BookQueryParser` | repositories, the database |
| `LibraryService` | `BookRepository`, `MemberRepository`, `Clock`, `LateFeePolicy`, `EventPublisher` | concrete databases, proxies, decorators, listeners, the waiting list |
| `LibraryCirculation` | `BookRepository`, `WaitingList`, `Cashier`, `Notifier` | the service, the console |
| `Book`, `Loan` | nothing outside the domain | everything else |
| `JdbcBookRepository` | `BookDao`, `LoanDao`, `DataSource`, domain | the service |
| `MongoBookRepository` | `MongoDatabase`, domain | the service |
| `LibraryApp` | **everything** | — |

Every dependency of the service and the mediator is an **interface** passed in through the constructor. The only place that knows about concrete classes is `LibraryApp`.

### 3.4 Wiring the Application

`LibraryApp` is the **composition root**: it creates every object, composes them and injects them through constructors. It offers three variants:

```java
LibraryApp.inMemory()     // books and members in memory
LibraryApp.withMySql()    // books and members in MySQL
LibraryApp.withMongo()    // books in MongoDB, members in MySQL
```

The shared part lives in the `assemble` method, so all three variants behave the same way. The MongoDB variant, with every layer of wrapping:

```java
BookRepository books =
    TimingProxy.wrap(BookRepository.class,                        // Proxy: timing
        new LazyBookRepository(                                   // Proxy: lazy creation
            () -> new MongoBookRepository(database)));            // Repository / Adapter

LateFeePolicy feePolicy =
    new MaxFeeDecorator(new StandardFeePolicy(), config.maxFee()); // Decorator + Strategy + Singleton

var circulation = new LibraryCirculation(books, new WaitingList(),
                                         new Cashier(), new ConsoleNotifier());   // Mediator + Adapter

var events = new EventPublisher();                                // Observer
events.subscribe(new ActivityLogListener());
events.subscribe(new StatisticsListener());
events.subscribe(circulation);

var service = new LibraryService(books, members, clock, feePolicy, events);      // Facade
```

The `Main` classes boil down to:

```java
try (var app = LibraryApp.withMongo()) {
    new ConsoleApp(app.service(), app.circulation()).run();
}
```

---

## 4. Flows Through the Application

Three flows show how the patterns work together. Each step is labeled with the pattern that performs it.

### 4.1 Borrowing a Book

```
ConsoleApp ──borrowBook──► LibraryService                         [Facade]
                              │
                              ├─ members.findById, books.findById [Repository]
                              │     └─ TimingProxy → (Lazy) → Jdbc/Mongo        [Proxy, Adapter]
                              │           └─ BookDao + LoanDao → assemble      [DAO]
                              ├─ limit and overdue checks (service)
                              ├─ book.borrow → Loan.start                      [Factory Method]
                              ├─ books.save → commit / rollback                [Unit of Work]
                              └─ events.publish(BookBorrowed)                  [Observer]
                                    ├─ ActivityLogListener (log)
                                    ├─ StatisticsListener (counting)
                                    └─ LibraryCirculation (removes from waiting list)  [Mediator]
```

### 4.2 Returning a Book with a Fee and a Waiting List

```
ConsoleApp ──returnBook──► LibraryService
                              ├─ book.returnBook (member check)
                              ├─ books.save                                    [Unit of Work]
                              ├─ feePolicy.calculate(daysLate)                 [Strategy]
                              │     └─ MaxFeeDecorator caps it at 500          [Decorator]
                              └─ events.publish(BookReturned)                  [Observer]
                                    └─ LibraryCirculation                      [Mediator]
                                          ├─ Cashier.charge
                                          │     └─ mediator.debtChanged ─► Notifier (debt, warning)
                                          └─ WaitingList.pollNext ─► Notifier (book available)
                                                                   └─ ConsoleNotifier  [Adapter]
```

### 4.3 Searching

```
ConsoleApp: "genre:KRIMI AND NOT pages>250"
   │
   ├─ BookQueryParser.parse(text)                      → expression tree  [Interpreter, Composite]
   │      And(GenreIs(KRIMI), Not(PagesGreaterThan(250)))
   │
   └─ LibraryService.search(expression)
          └─ for (Book b : catalog())                                   [Iterator]
                 └─ books.findPage(0, 2), findPage(1, 2), ...            [Repository, Proxy]
                 └─ expression.interpret(b)                              [Interpreter]
```

---

## 5. Design Patterns

Each pattern is described in the same order: **definition**, the **problem** it addressed, the **solution** in the project, **how it works**, **what we gained** and **notes**.

### At a Glance

| Group | Pattern | Classes in the project |
|---|---|---|
| Architectural | Repository | `BookRepository` + `InMemory`, `Jdbc`, `Mongo` |
| | DAO | `BookDao`, `LoanDao`, `MemberDao`, `ShelfDao` |
| | Unit of Work | `JdbcBookRepository.save` |
| Creational | Singleton | `LibraryConfig` |
| | Factory Method | `Book.restore`, `Loan.start`, `LibraryApp.withMongo` |
| | Builder | `Book.builder()` |
| Structural | Adapter | `Jdbc/MongoBookRepository`, `ConsoleNotifier`, `BookQueryParser` |
| | Decorator | `MaxFeeDecorator` |
| | Proxy | `TimingProxy`, `LazyBookRepository` |
| | Facade | `LibraryService`, `LibraryApp` |
| | Composite | expression tree (`And`, `Or`, `Not`) |
| Behavioral | Strategy | `LateFeePolicy` |
| | Observer | `EventPublisher` + listeners |
| | Mediator | `LibraryCirculation` |
| | Iterator | `BookCatalog` |
| | Interpreter | `BookExpression` + `BookQueryParser` |

---

### 5.1 Architectural Patterns

#### Repository

**Definition.** A repository is an abstraction that presents itself to business logic as a collection of domain objects and hides where and how those objects are actually stored. Business code only finds and saves objects through an interface, without knowing whether a database, a file, memory or an external service sits underneath.

**Problem.** Without a repository, the service would mix business rules with SQL queries and connections. It could not be tested without a database, and changing the database would mean changing business logic.

**Solution.** `BookRepository` is a domain interface that behaves like a collection of books. It has three implementations: `InMemoryBookRepository` (a map), `JdbcBookRepository` (MySQL) and `MongoBookRepository` (MongoDB).

**How it works.**

```java
public interface BookRepository {
    Optional<Book> findById(long id);
    List<Book> findAvailable();
    List<Book> findBorrowedBy(long memberId);
    List<Book> findOverdue(LocalDate today);
    List<Book> findPage(int page, int size);
    void save(Book book);
    // ...
}
```

Methods are named in the language of the library. Implementations assemble the aggregate when reading and take it apart when saving.

**Gained.** `LibraryService` is identical in all three variants of the application. Service tests run in memory, without Docker.

**Notes.** A repository is not tied to a database: the in-memory implementation is just as valid. Repositories exist only for aggregate roots (book, member, shelf), not for loans.

#### DAO (Data Access Object)

**Definition.** A DAO is an object that hides the technical details of accessing a single data source, usually one table. It runs queries against that source and returns raw data shaped like its structure, without any knowledge of business rules.

**Problem.** In MySQL the book aggregate is split across two tables. Code that reads one table should not be mixed with code that assembles the whole aggregate.

**Solution.** `BookDao` and `LoanDao` each know one table and return raw rows (`BookRow`, `LoanRow`). `JdbcBookRepository` uses them and builds a `Book` from the rows.

**How it works.**

```java
private Book assemble(BookRow row) {
    Loan latestLoan = loanDao.selectLatestByBookId(row.id())
        .map(l -> new Loan(l.id(), l.memberId(), l.borrowedAt(), l.dueDate(), l.returnedAt()))
        .orElse(null);
    return Book.restore(row.id(), row.title(), row.author(), row.pages(),
                        Genre.valueOf(row.genre()), row.shelfId(), latestLoan);
}
```

**Gained.** Each DAO has a single responsibility, and the repository speaks the language of the domain. `SQLException` is translated into `DataAccessException`, so the technology never leaks out of infrastructure.

**Notes.** A DAO abstracts a table; a repository abstracts a collection of domain objects. The MongoDB implementation does not need DAOs, because the aggregate is a single document.

#### Unit of Work

**Definition.** Unit of Work is a pattern that tracks every change made to objects during one business operation and writes them to storage together, as a single unit. If any part fails, nothing is written: all or nothing.

**Problem.** Borrowing a book changes two tables. If the book write succeeds and the loan write fails, the database becomes inconsistent.

**Solution.** `JdbcBookRepository.save` writes the book and its loan in a single transaction. DAO write methods take the connection as a parameter so they can share the same transaction.

**How it works.**

```java
conn.setAutoCommit(false);
try {
    bookDao.update(conn, bookRow);
    loanDao.upsert(conn, loanRow);
    conn.commit();
} catch (RuntimeException e) {
    conn.rollback();
    throw e;
}
```

**Gained.** All or nothing: a book is never left without its loan. The repository runs the transaction, because only the repository knows what makes up the aggregate.

**Notes.** MongoDB needs no explicit transaction here: writing a single document is atomic, and the whole aggregate is one document. In Spring, `@Transactional` does the same job.

---

### 5.2 Creational Patterns

#### Singleton

**Definition.** Singleton is a creational pattern that ensures a class has exactly one instance in the whole application and provides a global point of access to it. It is achieved with a private constructor, a static field holding the instance and a static method that returns it.

**Problem.** Database URLs, passwords and fee limits were written in several places. Changing the database port meant hunting down every occurrence.

**Solution.** `LibraryConfig` loads `library.properties` once, and everyone uses the same instance.

**How it works.** The holder idiom: the instance is created on first use, and the JVM guarantees thread safety without locking.

```java
public final class LibraryConfig {
    private LibraryConfig() { /* load the file */ }

    private static class Holder {
        private static final LibraryConfig INSTANCE = new LibraryConfig();
    }

    public static LibraryConfig getInstance() {
        return Holder.INSTANCE;
    }
}
```

**Gained.** Settings live in one place and can be changed without recompiling.

**Notes.** A singleton is global state and makes testing harder. That is why `getInstance()` is called only in `LibraryApp`, and the values are passed on through constructors. The rest of the application does not know the singleton exists.

#### Factory Method (static factory methods)

**Definition.** Factory Method is a creational pattern that defines a method for creating objects and lets subclasses decide which concrete class to instantiate. A static factory method is a related technique: a static method with a meaningful name that replaces a constructor and can return an existing object or a subtype.

**Problem.** A book is created in two different situations: a new book arrives at the library, or the repository restores an existing one from the database together with its loan. Two constructors with similar parameters would be confusing.

**Solution.** Named static methods: `Book.restore(...)` for an existing book, `Loan.start(...)` for a new loan, `LibraryApp.withMongo()` for wiring an application variant.

**How it works.**

```java
public static Book restore(long id, String title, ..., Loan latestLoan) {
    return new Book(id, title, ..., latestLoan);
}

static Loan start(long memberId, LocalDate today, int loanDays) {
    return new Loan(UUID.randomUUID(), memberId, today, today.plusDays(loanDays), null);
}
```

**Gained.** The intent is clear from the name. `Loan.start` is not public, so only a book can create a loan.

**Notes.** These are static factory methods, not the GoF Factory Method where subclasses decide what gets created. They solve the same problem: object creation is separated and named.

#### Builder

**Definition.** Builder is a creational pattern that lets you construct a complex object step by step, through named methods, instead of a constructor with many parameters. At the end, `build()` validates the data and creates the object.

**Problem.** The book constructor has six parameters, two of which are `long` (the book id and the shelf id). They are easy to swap, and the compiler won't notice.

**Solution.** `Book.builder()` with named methods and validation in `build()`.

**How it works.**

```java
Book book = Book.builder()
    .id(1).title("Na Drini ćuprija").author("Ivo Andrić")
    .pages(380).genre(Genre.ROMAN).shelfId(1)
    .build();
```

`build()` checks the required fields and delegates to the existing private constructor, so the same rules apply as for every other way of creating a book.

**Gained.** Every value is named and cannot be swapped by accident. A book never exists in an invalid state. Used in `LibraryApp.inMemory()` and in data adapters.

**Notes.** Required fields are checked at runtime, not at compile time. Small objects such as `Member(id, name)` do not need a builder.

---

### 5.3 Structural Patterns

#### Adapter

**Definition.** Adapter is a structural pattern that lets objects with incompatible interfaces work together. It wraps an existing object and translates its interface into the one the client expects, without changing either the client or the existing object.

**Problem.** The domain expects its own interfaces (`BookRepository`, `Notifier`), while technologies offer theirs (SQL, Mongo documents, the console). The user types text, while the service expects domain objects.

**Solution.** Classes that translate one interface into another: `JdbcBookRepository` and `MongoBookRepository` translate the domain into a database, `ConsoleNotifier` turns notifications into console output, `BookQueryParser` turns text into expressions, and `ConsoleApp` turns keyboard input into service calls.

**How it works.**

```java
public class ConsoleNotifier implements Notifier {
    public void send(long memberId, String message) {
        System.out.println("   [notification → member " + memberId + "] " + message);
    }
}
```

**Gained.** The domain knows nothing about technology. A new technology is a new adapter: an `EmailNotifier` could replace `ConsoleNotifier` without any change to the mediator.

**Notes.** This is the "Ports and Adapters" architecture: the domain interface is the port, the implementations are adapters. An adapter changes the interface; if it keeps the interface and adds behavior, it is a decorator.

#### Decorator

**Definition.** Decorator is a structural pattern that adds behavior to an object by wrapping it in another object with the same interface. The wrapper forwards calls to the original and does its own work before or after, and decorators can be stacked inside one another.

**Problem.** A new rule said that a fee must not exceed 500 RSD. Adding it to every policy would duplicate code, and new classes (`CappedStandard...`, `CappedStudent...`) would multiply.

**Solution.** `MaxFeeDecorator` wraps any fee policy.

**How it works.**

```java
public class MaxFeeDecorator implements LateFeePolicy {
    private final LateFeePolicy inner;
    private final long maximum;

    public long calculate(long daysLate) {
        return Math.min(inner.calculate(daysLate), maximum);
    }
}

new MaxFeeDecorator(new StandardFeePolicy(), 500);
```

**Gained.** The cap applies to every policy, and no policy was changed. Decorators can be stacked.

**Notes.** Strategy decides how the fee is calculated; Decorator adds a rule around it. Java uses the same pattern in `Collections.unmodifiableList` and `BufferedReader`.

#### Proxy

**Definition.** Proxy is a structural pattern that provides a stand-in for another object, with the same interface, in order to control access to it. It is used for lazy creation, access control, caching, remote calls and extra work around calls, such as timing and transactions.

**Problem.** We wanted to measure how long database calls take and to postpone connecting to MongoDB, without changing any repository.

**Solution.** Two proxies. `TimingProxy` is a dynamic proxy that times every call on any interface. `LazyBookRepository` is a virtual proxy that creates the real repository only on the first call.

**How it works.**

```java
public static <T> T wrap(Class<T> type, T target) {
    InvocationHandler handler = (proxy, method, args) -> {
        long start = System.nanoTime();
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();                 // rethrow the original exception
        } finally {
            System.out.printf("   [%s.%s] %.2f ms%n", ...);
        }
    };
    return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
}
```

**Gained.** One class of about ten lines times every repository, and it automatically works for new methods (for example `findPage`). The console shows the speed difference between MySQL and MongoDB.

**Notes.** Spring uses the same mechanism for `@Transactional`. That is why the annotation does not work when a method calls another method of the same class: a call through `this` bypasses the proxy. A proxy controls access, a decorator adds behavior; their structure is the same.

#### Facade

**Definition.** Facade is a structural pattern that provides a simple, unified interface to a complex subsystem made of several classes. The client calls the facade, and the facade coordinates all the necessary parts internally, without forbidding direct access to the subsystem.

**Problem.** The console would have to know about repositories, rule checks, saving and events. The `Main` classes each had about twenty lines of wiring, repeated in every one of them.

**Solution.** `LibraryService` is a facade over the domain, and `LibraryApp` is a facade over application wiring.

**How it works.** The console calls `library.borrowBook(memberId, bookId)`, and behind it the service coordinates loading, checking, saving and publishing events. `LibraryApp.withMongo()` hides some fifteen classes behind a single call.

**Gained.** A simple entry point into a complex system, with shared code in one place. `LibraryApp` implements `AutoCloseable`, so resources are closed automatically.

**Notes.** A facade does not forbid direct access to the subsystem; tests still work directly with the classes. The risk is that a facade grows into a "God object", which is why there are two separate facades.

#### Composite

**Definition.** Composite is a structural pattern that arranges objects into a tree, so that individual objects and groups of objects are used the same way, through a common interface.

**Problem.** Search conditions must be combinable to any depth: `A AND (B OR NOT C)`.

**Solution.** `And`, `Or` and `Not` contain other expressions and are expressions themselves.

**How it works.**

```java
public record And(BookExpression left, BookExpression right) implements BookExpression {
    public boolean interpret(Book book) {
        return left.interpret(book) && right.interpret(book);
    }
}
```

**Gained.** A simple condition and a complex tree are used the same way, through `interpret`.

**Notes.** Here, Composite is the structure the Interpreter is built on.

---

### 5.4 Behavioral Patterns

#### Strategy

**Definition.** Strategy is a behavioral pattern that extracts a family of algorithms into separate classes with a common interface, making them interchangeable. The class that uses the algorithm depends only on the interface, and the concrete strategy is supplied from outside.

**Problem.** A fee can be calculated in several ways (standard, student, no fee). Without the pattern, this would be an `if-else` in the service that grows with every new rule.

**Solution.** The `LateFeePolicy` interface with `StandardFeePolicy`, `StudentFeePolicy` and `NoFeePolicy` implementations. The service receives the policy through its constructor.

**How it works.**

```java
public interface LateFeePolicy {
    long calculate(long daysLate);
}

// in the service
return feePolicy.calculate(daysLate);
```

**Gained.** A new policy is a new class, and the service stays unchanged. Each policy is tested on its own. Since the interface has one method, a policy can also be a lambda: `daysLate -> 0`.

**Notes.** Java uses the same pattern in `Comparator`. The difference from Template Method: Strategy swaps the algorithm through composition, not inheritance.

#### Observer

**Definition.** Observer is a behavioral pattern in which a subject keeps a list of observers and automatically notifies them when an event occurs. The subject depends only on the observer interface, so new observers can be added without changing the subject.

**Problem.** After a book is borrowed or returned, several things need to react: logging, statistics, the waiting list, debts. Every new reaction would change the service.

**Solution.** The service publishes `BookBorrowed` and `BookReturned` events through `EventPublisher`, and observers subscribe to them.

**How it works.**

```java
public void publish(LibraryEvent event) {
    for (LibraryEventListener listener : listeners) {
        try {
            listener.onEvent(event);
        } catch (RuntimeException e) {
            System.err.println("   [events] listener failed: " + e.getMessage());
        }
    }
}
```

**Gained.** The service does not know who reacts. The waiting list and debts were added without a single change to the service.

**Notes.** An event is published only after a successful `save`. The `try-catch` keeps one broken observer from breaking the others, and `CopyOnWriteArrayList` allows subscribing during notification. In Spring: `ApplicationEventPublisher` and `@TransactionalEventListener`.

#### Mediator

**Definition.** Mediator is a behavioral pattern that removes direct dependencies between objects by routing all of their communication through a mediator. The mediator holds the collaboration logic, and the participants know only about it.

**Problem.** The waiting list, the cashier and notifications have to work together: a returned book charges a fee, debt may trigger a warning, and the first person on the waiting list should be notified. If the components called each other, the logic would be scattered across all of them.

**Solution.** `LibraryCirculation` is the mediator. The components (`WaitingList`, `Cashier`, `Notifier`) do not know about each other, and all collaboration rules live in the mediator.

**How it works.**

```java
private void bookReturned(LibraryEvent.BookReturned event) {
    if (event.fee() > 0) {
        cashier.charge(event.memberId(), event.fee());     // the cashier reports debtChanged back
    }
    waitingList.pollNext(event.bookId()).ifPresent(next ->
        notifier.send(next, "Book \"" + event.title() + "\" is available again."));
}

@Override
public void debtChanged(long memberId, long totalDebt) {
    notifier.send(memberId, "Total debt: " + totalDebt + " RSD.");
    if (totalDebt > DEBT_WARNING) {
        notifier.send(memberId, "Warning: debt exceeds " + DEBT_WARNING + " RSD.");
    }
}
```

**Gained.** Communication is two-way: the cashier tells the mediator that a debt changed, and the mediator decides what that means. Changing a rule (for example, a different debt threshold) changes only the mediator.

**Notes.** The mediator receives events as an observer, so here the Mediator builds on the Observer. The difference: an observer only reacts, while a mediator makes decisions and gives orders to components.

#### Iterator

**Definition.** Iterator is a behavioral pattern that provides sequential access to the elements of a collection without exposing its internal structure. In Java it consists of the `Iterable` and `Iterator` interfaces, and the `for-each` loop uses it automatically.

**Problem.** `findAll()` loads every book at once, which does not scale for a large library. Code that walks through books should not need to know about pages.

**Solution.** `BookCatalog implements Iterable<Book>`, with an iterator that loads books page by page through `findPage`.

**How it works.**

```java
public boolean hasNext() {
    while (!current.hasNext() && !lastPageLoaded) {
        List<Book> page = books.findPage(nextPage++, pageSize);
        lastPageLoaded = page.size() < pageSize;
        current = page.iterator();
    }
    return current.hasNext();
}

// usage
for (Book b : library.catalog()) { ... }
```

**Gained.** From the outside it is a plain `for-each` loop, and pages are loaded only when needed. Thanks to `TimingProxy`, the console shows queries interleaved with the output.

**Notes.** `findPage` had to be added to every repository implementation (except the dynamic proxy), which shows the cost of a large interface (Interface Segregation). `ResultSet` and MongoDB's `FindIterable` are iterators too.

#### Interpreter

**Definition.** Interpreter is a behavioral pattern that defines the grammar of a simple language as a set of classes and represents each expression as a tree of objects. An expression is evaluated by having each node of the tree evaluate itself, with composite nodes combining the results of their children.

**Problem.** Searching by arbitrary combinations of conditions would require endless methods such as `findByAuthorAndPagesGreaterThan`.

**Solution.** A small query language. Each condition is a class (`AuthorContains`, `GenreIs`, `PagesGreaterThan`, `IsAvailable`), and `And`, `Or` and `Not` combine them. `BookQueryParser` turns text into an expression tree.

**How it works.**

```
"genre:KRIMI AND NOT pages>250"
        │  BookQueryParser (recursive descent: OR → AND → NOT → condition)
        ▼
And(GenreIs(KRIMI), Not(PagesGreaterThan(250)))
        │  interpret(book) for every book in the catalog
        ▼
list of matching books
```

**Gained.** A new condition is a new class plus one line in the parser. Expressions are records, so tests compare entire trees with `assertEquals`. The service takes an expression, not text, so a different input (for example JSON) would use a different parser while the service stays the same.

**Notes.** Expressions live in the domain (the meaning of a condition), the parser in the application layer (the input format). Filtering happens in memory; a better approach would be another interpretation of the same tree, `toSql()` or `toMongoFilter()`, so the database does the filtering.

---

## 6. SOLID Principles in the Project

SOLID is a set of five object-oriented design principles popularized by Robert C. Martin. They are not patterns, but rules that describe what makes code easy to change; patterns are concrete solutions that follow those rules.

- **S – Single Responsibility:** a class should have only one reason to change, meaning it answers to only one kind of requirement.
- **O – Open/Closed:** a class should be open for extension but closed for modification; new behavior is added with new code, not by changing existing code.
- **L – Liskov Substitution:** every implementation or subclass must be able to replace its supertype without breaking the program; it must not require more, guarantee less or throw unexpected exceptions.
- **I – Interface Segregation:** a client should not depend on methods it does not use; several small, specific interfaces are better than one large one.
- **D – Dependency Inversion:** high-level and low-level modules depend on abstractions, not on each other; the interface is defined by whoever needs it and implemented by whoever fulfills it.

| Principle | Where it shows |
|---|---|
| **S** – Single Responsibility | `Book` (rules), `JdbcBookRepository` (database), `ConsoleApp` (output), `LibraryService` (flow), each DAO one table, each observer one reaction, `WaitingList` / `Cashier` / `Notifier` one job each |
| **O** – Open/Closed | a new fee policy (Strategy), a new event reaction (Observer), the fee cap (Decorator), a new database (Repository), the waiting list added without changing the service |
| **L** – Liskov Substitution | all three repository implementations are interchangeable. A subtle exception: the in-memory one returns the same object, so it hides a forgotten `save` |
| **I** – Interface Segregation | `LateFeePolicy`, `Notifier` and `LibraryEventListener` have one method each and can be lambdas. `BookRepository` with eight methods shows the opposite: `findPage` required changes in four classes |
| **D** – Dependency Inversion | the service and the mediator depend only on interfaces; the interfaces live in the domain; only `LibraryApp` knows concrete classes |

Singleton is the only pattern in tension with SOLID (global state), so it is confined to the composition root.

---

## 7. Tests

| Class | What it tests |
|---|---|
| `BookTest` | book and loan rules |
| `LibraryServiceTest` | service rules, with a fixed `Clock` |
| `LateFeePolicyTest` | fee policies and the decorator, including stacked decorators |
| `TimingProxyTest` | that the proxy returns the same result and the original exception |
| `LazyBookRepositoryTest` | laziness and single creation |
| `BookCatalogTest` | iterating over all books and loading pages only when needed |
| `BookExpressionTest` | evaluating simple and composite expressions |
| `BookQueryParserTest` | operator precedence, parentheses, error messages |
| `LibraryCirculationTest` | waiting list, notifications, debt and warnings |

All tests run without a database. Components that depend on interfaces are tested with in-memory implementations or lambdas (for example a `Notifier` that just records messages).

---

## 8. Limitations and Future Work

- **The waiting list is not a reservation.** The first person on the list is notified, but anyone can still borrow the book. Real reservations need new state in `Book` and new columns in both databases.
- **The waiting list and debts live in memory** and are lost when the program exits.
- **Search filters in memory.** Translating the expression tree into SQL and a MongoDB filter would move filtering into the database.
- **N+1 in JDBC.** `assemble` runs a separate query for each book's loan.
- **Clock in `returnBook`.** Lateness is calculated with `LocalDate.now()` instead of the injected `Clock`.
- **Observers live in the domain.** `ActivityLogListener` and `StatisticsListener` would fit better in infrastructure, and `EventPublisher` in the application layer.
- **No integration tests** against a real database; Testcontainers would be the natural next step.
- **Next step:** the same project in Spring Boot, where `@Transactional`, Spring Data and `ApplicationEventPublisher` replace the hand-written parts.
