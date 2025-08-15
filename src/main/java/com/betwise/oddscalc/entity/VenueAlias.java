package com.betwise.oddscalc.entity;

public record VenueAlias(
        VenueKey aliasKey,
        int      venueId
) {
    public String aliasGroundName() {
        return aliasKey.groundName();
    }
    public String aliasCity() {
        return aliasKey.city();
    }
}