/**
 * @author Owen Walton
 * Container class that stores the data required to identify a unique team obejct,
 * TeamKey refers to the 'natural key' of a team,
 * so that if the teamID is unknown, it can still be referenced as a foreign key in other objects
 *
 * Java Record is suitable because in order to maintain data relationships, it should be immutable + easily comparible
 */

package com.betwise.oddscalc.entity;

import java.util.Objects;

public record TeamKey(String name) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamKey that)) return false;
        return Objects.equals(
                name == null ? null : name.trim().toLowerCase(),
                that.name == null ? null : that.name.trim().toLowerCase()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(name == null ? null : name.trim().toLowerCase());
    }
}