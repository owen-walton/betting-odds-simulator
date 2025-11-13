/**
 * @author Owen Walton
 * Container class that stores the data required to identify a unique venue obejct,
 * VenueKey refers to the 'natural key' of a venue,
 * so that if the venueID is unknown, it can still be referenced as a foreign key in other objects
 *
 * Java Record is suitable because in order to maintain data relationships, it should be immutable + easily comparible
 */

package com.betwise.oddscalc.entity;

import java.util.Objects;

public record VenueKey(String groundName, String city) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VenueKey that)) return false;
        return Objects.equals(groundName, that.groundName)
                && Objects.equals(city,   that.city);
    }

    @Override
    public int hashCode() {
        // Follows the standard recipe: combine field hashes
        return Objects.hash(groundName, city);
    }
}