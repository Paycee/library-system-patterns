db = db.getSiblingDB('biblioteka');

db.books.insertMany([
    { _id: 1, title: 'Na Drini ćuprija',           author: 'Ivo Andrić',   pages: 380, genre: 'ROMAN', shelfId: 1, latestLoan: null },
    { _id: 2, title: 'Prokleta avlija',            author: 'Ivo Andrić',   pages: 150, genre: 'ROMAN', shelfId: 1, latestLoan: null },
    { _id: 3, title: 'Ubistvo u Orijent ekspresu', author: 'Agata Kristi', pages: 260, genre: 'KRIMI', shelfId: 2, latestLoan: null },
    { _id: 4, title: 'Deset malih crnaca',         author: 'Agata Kristi', pages: 240, genre: 'KRIMI', shelfId: 2, latestLoan: null }
]);