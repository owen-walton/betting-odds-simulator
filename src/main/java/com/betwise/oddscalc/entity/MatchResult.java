package com.betwise.oddscalc.entity;

public record MatchResult(
        int matchID,
        Integer winningTeamID, // nullable for draw/no result
        int tossWinningTeamID,
        TossDecision tossDecision,
        Integer marginSize, // nullable for draw/no result
        MarginType marginType,
        Result result
) { }
