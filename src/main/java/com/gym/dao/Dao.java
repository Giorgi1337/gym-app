package com.gym.dao;

import java.util.List;

public interface Dao<T, ID> {

    void save(ID id, T entity);

    T findById(ID id);

    List<T> findAll();

    void delete(ID id);

    default boolean exists(ID id) {
        return findById(id) != null;
    }
}