package com.gym.dao;

import java.util.List;
import java.util.Set;

public interface Dao<T, ID> {

    void save(ID id, T entity);

    T findById(ID id);

    List<T> findAll();

    void delete(ID id);

    default boolean exists(ID id) {
        return findById(id) != null;
    }

    default Set<ID> findAllIds() {
        throw new UnsupportedOperationException("findAllIds() not implemented");
    }
}