package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.dao.MatchResultDAO;
import com.betwise.oddscalc.entity.*;

import java.util.HashMap;
import java.util.Map;

public class PredictionService {

    // predicts the chance of each team winning and draw chance
    // match schema passed in must contain 2 teams,
    public Map<Team, Double> predict(CricketMatchDataSchema match, PredictionModel model) {
        try (MatchResultDAO matchResultDAO = new MatchResultDAO()) {

            Team teamA = match.getTeams().get(0);
            Team teamB = match.getTeams().get(1);

            // Adjust ELO ratings for home-field advantage
            double adjEloA = adjustELO(teamA, match, model);
            double adjEloB = adjustELO(teamB, match, model);
            if (adjEloA == -1 || adjEloB == -1) {
                // if a team has no ELO, don't allow odds on the game
                return null;
            }
            teamA.setElo((float) adjEloA);
            teamB.setElo((float) adjEloB);

            // Core ELO win probability
            double teamAWinChance = ELOformula(adjEloA, adjEloB, model.getEValue());

            // Historical draw frequency
            double drawChance = matchResultDAO.getDrawPercentage();

            Map<Team, Double> results = new HashMap<>();
            results.put(null, drawChance); // null = draw
            results.put(teamA, teamAWinChance - drawChance / 2);
            results.put(teamB, (1.0 - teamAWinChance) - drawChance / 2);

            return results;
        }
    }

    private double ELOformula(double teamELO, double opponentELO, double eValue) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentELO - teamELO) / eValue));
    }

    /**
     * Adjusts a single team’s ELO for prediction using known pre-match factors.
     * Only home-field advantage is currently applied because toss and margin, etc. are not
     * known before the match the so cannot affect the probability
     */
    public double adjustELO(Team team, CricketMatchDataSchema match, PredictionModel model) {
        double adjusted = team.getElo();
        if (team.getElo() == null) {
            return -1;
        }
        for (TeamHomeVenue hv : match.getTeamHomeVenues()) {
            if (team.getTeamID() == hv.teamID()) {
                adjusted *= model.getHomeAdvantageMultiplier();
            }
        }
        return adjusted;
    }

    /**
     * After matches finish, update team ELOs using the result and the
     * latest prediction model.  Uses TossWinnerEloGainMultiplier and
     * WinMarginMultiplier when applicable.
     */
    /* public void updateELOsFor(Map<DataSource, String> matchIDs) {
        try (CricketMatchDAO matchDAO = new CricketMatchDAO();
             TeamDAO teamDAO = new TeamDAO();
             MatchResultDAO resultDAO = new MatchResultDAO();
             PredictionModelDAO modelDAO = new PredictionModelDAO()) {

            PredictionModel model = modelDAO.getLatestModel();

            for (Map.Entry<DataSource, String> entry : matchIDs.entrySet()) {
                CricketMatchDataSchema schema = matchDAO.getMatch(entry);
                if (schema == null || schema.getTeams().size() < 2) continue;

                Team tA = schema.getTeams().get(0);
                Team tB = schema.getTeams().get(1);
                List<MatchResult> results = schema.getMatchResults();
                if (results.isEmpty()) continue;

                MatchResult r = results.get(0);

                double eloA = tA.getElo() == null ? 1500.0 : tA.getElo();
                double eloB = tB.getElo() == null ? 1500.0 : tB.getElo();

                double expA = ELOformula(eloA, eloB, model.geteValue());
                double expB = 1.0 - expA;

                // Actual scores: 1 win, 0 loss, 0.5 draw
                double scoreA, scoreB;
                if (r.getResult() == Result.DRAW || r.getResult() == Result.TIE) {
                    scoreA = scoreB = 0.5;
                } else if (r.getWinningTeamID() != null &&
                        r.getWinningTeamID() == tA.getTeamID()) {
                    scoreA = 1.0; scoreB = 0.0;
                } else if (r.getWinningTeamID() != null &&
                        r.getWinningTeamID() == tB.getTeamID()) {
                    scoreA = 0.0; scoreB = 1.0;
                } else {
                    continue;
                }

                // Base K from model
                double k = model.getEloGain();

                // If toss winner is known and matches the team, scale K
                    if (r.getTossWinningTeamID() == tA.getTeamID()) {
                        k *= model.getTossWinnerEloGainMultiplier();
                    } else if (r.getTossWinningTeamID() == tB.getTeamID()) {
                        k *= model.getTossWinnerEloGainMultiplier();
                    }

                // Apply margin multiplier if margin known
                if (r.getMarginSize() != null && r.getMarginSize() > 0) {
                    k *= model.getWinMarginMultiplier();
                }

                double newA = eloA + k * (scoreA - expA);
                double newB = eloB + k * (scoreB - expB);

                teamDAO.updateElo(new TeamKey(tA.getName()), (float) newA);
                teamDAO.updateElo(new TeamKey(tB.getName()), (float) newB);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ELOs", e);
        }
    }*/
}
