package com.betwise.oddscalc.entity;

import java.util.List;

public record CricketMatchDataSchema(
        List<MatchFormat> matchFormats,
        List<Team> teams,
        List<Venue> venues,
        List<TeamHomeVenue> teamHomeVenues,
        List<CricketMatch> cricketMatches,
        List<MatchResult> matchResults,
        List<MatchTeam> matchTeams
) {
    public void appendSchema(CricketMatchDataSchema partialSchema) {
        this.matchFormats.addAll(partialSchema.matchFormats());
        this.teams.addAll(partialSchema.teams());
        this.venues.addAll(partialSchema.venues());
        this.teamHomeVenues.addAll(partialSchema.teamHomeVenues());
        this.cricketMatches.addAll(partialSchema.cricketMatches());
        this.matchResults.addAll(partialSchema.matchResults());
        this.matchTeams.addAll(partialSchema.matchTeams());
    }
}
