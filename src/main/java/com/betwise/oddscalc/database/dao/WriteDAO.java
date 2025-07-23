package com.betwise.oddscalc.database.dao;

import java.util.List;

public interface WriteDAO<T> {
    public boolean insert(T t); // currently no duplicate handling
    public List<T> bulkInsertIfNotExists(List<T> t);
}
