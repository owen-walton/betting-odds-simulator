/**
 * Currently draws are considered negligible, potential to implement in future
 */
package com.betwise.oddscalc.ml;

import com.betwise.oddscalc.database.dao.CricketMatchDAO;
import com.betwise.oddscalc.entity.DataSource;
import com.betwise.oddscalc.entity.MatchDetails;
import com.betwise.oddscalc.entity.TuningParams;

import java.util.List;
import java.util.Map;

public class ObjectiveFunction {

    /**
     * the lower the return value the stronger the model is (the less average loss of prediction)
     */
    public double evaluateModel(TuningParams tuningParams) {
        final int BATCH_SIZE = 500;
        double totalLogLoss = 0.0;
        int totalMatches = 0;

        try (CricketMatchDAO cricketMatchDAO = new CricketMatchDAO()) {
            String latestMatchID = null;
            DataSource latestDataSource = null;
            // start from beginning if null
            while (true) {
                List<MatchDetails> batch = cricketMatchDAO.getMatchDetails(latestMatchID, latestDataSource, BATCH_SIZE);
                if (batch.isEmpty()) break;

                for (MatchDetails match : batch) {
                    Integer winningTeamID = match.getWinningTeamID();
                    // if match was not a draw
                    if (winningTeamID != null) {
                        double winChance = predictWinProbability(winningTeamID, match, tuningParams);
                        if (!(winChance > 0 && winChance < 1)) {
                            throw new IllegalStateException("Win probability is invalid");
                        }
                        totalLogLoss -= Math.log(winChance);
                        totalMatches ++;
                    }
                }

                latestMatchID = batch.get(batch.size() - 1).getMatchID();
                latestDataSource = DataSource.valueOf(batch.get(batch.size() - 1).getDataSource());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating model", e);
        }

        if (totalMatches == 0) {
            throw new RuntimeException("No matches to evaluate");
        }
        return totalLogLoss / totalMatches;
    }

    private double predictWinProbability(int teamID, MatchDetails match, TuningParams params) {
        int opponentID = 0;
        for (int id : match.getTeamHomeMap().keySet()) {
            if (id != teamID) {
                opponentID = id;
                break;
            }
        }
        if (opponentID == 0) {
            throw new IllegalArgumentException("Match must have exactly 2 teams");
        }

        float teamElo = params.startingELO();
        float opponentElo = params.startingELO();

        if (match.getTeamEloMap() != null) {
            if (match.getTeamEloMap().containsKey(teamID)) {
                teamElo = match.getTeamEloMap().get(teamID).floatValue();
            }
            if (match.getTeamEloMap().containsKey(opponentID)) {
                opponentElo = match.getTeamEloMap().get(opponentID).floatValue();
            }
        }

        if (match.getTeamHomeMap().get(teamID)) {
            teamElo *= params.homeAdvMultiplier();
        }

        if (teamID == match.getTossWinningTeamID()) {
            teamElo *= params.tossAdvMultiplier();
        }

        double rawProb = 1.0 / (1.0 + Math.pow(10.0, (opponentElo - teamElo) / params.eValue()));

        return rawProb;
    }
}