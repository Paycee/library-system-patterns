package com.library.infrastructure.jdbc;

import com.library.domain.shelf.Shelf;
import com.library.domain.shelf.ShelfRepository;
import com.library.infrastructure.jdbc.dao.ShelfDao;
import com.library.infrastructure.jdbc.dao.ShelfRow;

import java.util.List;
import java.util.Optional;

/** Jednostavan primer: jedan agregat = jedna tabela = jedan DAO. */
public class JdbcShelfRepository implements ShelfRepository {
    private final ShelfDao shelfDao;

    public JdbcShelfRepository(ShelfDao shelfDao) {
        this.shelfDao = shelfDao;
    }

    @Override
    public Optional<Shelf> findById(long id) {
        return shelfDao.selectById(id).map(r -> new Shelf(r.id(), r.label()));
    }

    @Override
    public List<Shelf> findAll() {
        return shelfDao.selectAll().stream().map(r -> new Shelf(r.id(), r.label())).toList();
    }

    @Override
    public void save(Shelf shelf) {
        shelfDao.upsert(new ShelfRow(shelf.getId(), shelf.getLabel()));
    }
}
