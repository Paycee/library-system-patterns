-- Početni podaci za vežbu.
SET NAMES utf8mb4;

INSERT INTO shelves (id, label) VALUES
    (1, 'ROMAN'),
    (2, 'KRIMI');

INSERT INTO books (id, title, author, pages, genre, shelf_id) VALUES
    (1, 'Na Drini ćuprija', 'Ivo Andrić', 380, 'ROMAN', 1),
    (2, 'Prokleta avlija', 'Ivo Andrić', 150, 'ROMAN', 1),
    (3, 'Ubistvo u Orijent ekspresu', 'Agata Kristi', 260, 'KRIMI', 2),
    (4, 'Deset malih crnaca', 'Agata Kristi', 240, 'KRIMI', 2);

INSERT INTO members (id, name) VALUES
    (1, 'Pera'),
    (2, 'Mika');
