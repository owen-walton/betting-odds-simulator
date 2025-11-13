/**
 * @author Owen Walton
 * Mirrors the Enum field in the MySQL database, as part of CricketMatch table's composite PK
 * ,
 * Means that the matchID can be determined by the data source,
 * so that the match can be found if data source causes any issues or bad data
 */
package com.betwise.oddscalc.entity;

public enum DataSource {
    CRICSHEET, CRICAPI
}