/**
 * @author Owen Walton
 * Implemented by all DAOs that need to insert to db
 * Object 'T' represents the corresponding container object e.g. for the TeamDAO implementation, T is a Team object
 * @note insert() isn't used in the program however may be useful in any future developments so not removed
 */

package com.betwise.oddscalc.database.dao;

import java.util.List;

public interface WriteDAO<T> {
    boolean insert(T t);
    void bulkInsertIfNotExists(List<T> t);
}
