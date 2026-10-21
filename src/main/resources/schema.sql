-- MySQL šema. Primeti: 4 tabele, ali 3 agregata (Book, Member, Shelf).
-- Tabela loans NEMA svoj repository: pozajmica je deo agregata Book.

CREATE TABLE shelves (
    id     BIGINT PRIMARY KEY,
    label  VARCHAR(50) NOT NULL              -- žanr ili slovo, npr. 'KRIMI' ili 'A'
);

CREATE TABLE books (
    id        BIGINT PRIMARY KEY,
    title     VARCHAR(200) NOT NULL,
    author    VARCHAR(200) NOT NULL,
    pages     INT NOT NULL,
    genre     VARCHAR(30) NOT NULL,
    shelf_id  BIGINT NOT NULL,
    FOREIGN KEY (shelf_id) REFERENCES shelves(id)
);

CREATE TABLE members (
    id    BIGINT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL
);

CREATE TABLE loans (
    id           CHAR(36) PRIMARY KEY,         -- UUID kao tekst: UUID.toString() / UUID.fromString()
    book_id      BIGINT NOT NULL,
    member_id    BIGINT NOT NULL,
    borrowed_at  DATE NOT NULL,
    due_date     DATE NOT NULL,
    returned_at  DATE NULL,                    -- NULL = knjiga još nije vraćena
    FOREIGN KEY (book_id)   REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(id),
    INDEX idx_loans_book (book_id, borrowed_at)
);
