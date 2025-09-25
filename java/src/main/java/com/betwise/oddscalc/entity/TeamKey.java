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