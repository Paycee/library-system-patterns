package com.library.domain.shelf;

import java.util.List;
import java.util.Optional;

public interface ShelfRepository {
    Optional<Shelf> findById(long id);
    List<Shelf> findAll();
    void save(Shelf shelf);
}
