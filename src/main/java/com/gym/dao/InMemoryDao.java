package com.gym.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InMemoryDao<T, ID> implements Dao<T, ID> {

    private final Map<ID, T> storage;

    public InMemoryDao(Map<ID, T> storage) {
        this.storage = storage;
    }

    @Override
    public void save(ID id, T entity) {
        storage.put(id, entity);
    }

    @Override
    public T findById(ID id) {
        return storage.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(ID id) {
        storage.remove(id);
    }
}
