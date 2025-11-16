/**
 * @author Owen Walton
 * Object 'T' represents the corresponding container object e.g. for the TeamDAO implementation, T is a Team object
 * ,
 * Implemented by Team and Venue, can occur in many match files but only a fixed number (Venue-1, Team-2) per match file
 * Whilst Team is 2:many not 1:many with matches, it is still fixedNum:many, so utilises these methods
 * ,
 * These functions are used when identifying if a (list of) object(s) has previouslly occured in the db or not
 * (hence it only applies to the fixedNum:many relationship)
 */

package com.betwise.oddscalc.database.dao;

import java.util.List;
import java.util.Set;

public interface OneToManyMatches<T> {
    List<T> getIDsIntoObjects(List<T> t);
    List<T> removeExisting(List<T> t);
    Set<T> getAll();
}