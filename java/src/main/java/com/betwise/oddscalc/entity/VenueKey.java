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