package com.betwise.oddscalc.database.dao;

public interface WriteDAO<T> {
    public boolean insert(T t);
}
