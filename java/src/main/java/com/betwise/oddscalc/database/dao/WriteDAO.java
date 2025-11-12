package com.betwise.oddscalc.database.dao;

import java.util.List;

public interface WriteDAO<T> {
    boolean insert(T t);
    void bulkInsertIfNotExists(List<T> t);
}
