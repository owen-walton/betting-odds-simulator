package com.betwise.oddscalc.entity;

import java.time.LocalDate;

public record CricketMatch(
        int matchID,
        DataSource dataSource,
        MatchFormat format,
        int venueID,
        LocalDate startDate,
        MatchResult matchResult
) {}