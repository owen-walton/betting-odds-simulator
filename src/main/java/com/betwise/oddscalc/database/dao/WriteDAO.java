package com.betwise.oddscalc.database.dao;

import java.util.List;

public interface WriteDAO<T> {
    public boolean insert(T t);
    public boolean bulkInsertIfNotExists(List<T> t);
}
