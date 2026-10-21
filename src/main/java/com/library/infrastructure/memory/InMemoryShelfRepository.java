package com.library.infrastructure.memory;

import com.library.domain.shelf.Shelf;
import com.library.domain.shelf.ShelfRepository;

import java.util.*;

public class InMemoryShelfRepository implements ShelfRepository {
    private final Map<Long, Shelf> storage = new LinkedHashMap<>();

    @Override public Optional<Shelf> findById(long id) { return Optional.ofNullable(storage.get(id)); }
    @Override public List<Shelf> findAll()             { return List.copyOf(storage.values()); }
    @Override public void save(Shelf shelf)            { storage.put(shelf.getId(), shelf); }
}
