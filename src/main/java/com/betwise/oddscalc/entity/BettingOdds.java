package com.betwise.oddscalc.entity;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

// represents betting odds for a match
public class BettingOdds {

    // bookmaker margin
    public static final double MARKET_OVERROUND = 1.05;

    private final Team team1;
    private final Team team2;
    private final double drawChance;

    private final double team1Odds;
    private final double team2Odds;
    private final double drawOdds;

    public BettingOdds(Map<Team, Double> resultPrediction) {
        // Extract probabilities
        Objects.requireNonNull(resultPrediction, "resultPrediction cannot be null");
        if (resultPrediction.size() < 2) {
            throw new IllegalArgumentException("Prediction map must contain at least two entries (teams and draw).");
        }

        Double drawProb = Optional.ofNullable(resultPrediction.get(null)).orElse(0.0);
        this.drawChance = drawProb;

        Team t1 = null;
        Team t2 = null;
        Double p1 = null;
        Double p2 = null;

        for (Map.Entry<Team, Double> entry : resultPrediction.entrySet()) {
            if (entry.getKey() == null) continue;
            if (t1 == null) {
                t1 = entry.getKey();
                p1 = entry.getValue();
            } else {
                t2 = entry.getKey();
                p2 = entry.getValue();
            }
        }

        if (t1 == null || t2 == null) {
            throw new IllegalArgumentException("Prediction map must contain two distinct teams.");
        }

        this.team1 = t1;
        this.team2 = t2;

        // Convert probabilities to bookmaker decimal odds with overround
        // Overround is applied proportionally to probabilities
        double totalProb = (p1 + p2 + drawProb);
        double scale = totalProb * MARKET_OVERROUND;

        double adjP1 = p1 / scale;
        double adjP2 = p2 / scale;
        double adjDraw = drawProb / scale;

        this.team1Odds = probToDecimal(adjP1);
        this.team2Odds = probToDecimal(adjP2);
        this.drawOdds  = probToDecimal(adjDraw);
    }

    private static double probToDecimal(double probability) {
        if (probability <= 0.0) {
            return Double.POSITIVE_INFINITY; // no chance -> infinite odds
        }
        // Decimal odds = 1 / probability, rounded to 2 decimals
        return Math.round((1.0 / probability) * 100.0) / 100.0;
    }

    public Team getTeam1() { return team1; }
    public Team getTeam2() { return team2; }
    public double getDrawChance() { return drawChance; }

    public double getTeam1Odds() { return team1Odds; }
    public double getTeam2Odds() { return team2Odds; }
    public double getDrawOdds()  { return drawOdds; }

    /**
     * Returns a human–readable string of all three betting options
     * with decimal odds and their underlying probabilities.
     */
    public String oddsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(team1.getName()).append(": ")
                .append(team1Odds).append(" (p=")
                .append(String.format("%.2f", 1.0 / team1Odds)).append(")\n");
        sb.append(team2.getName()).append(": ")
                .append(team2Odds).append(" (p=")
                .append(String.format("%.2f", 1.0 / team2Odds)).append(")\n");
        sb.append("Draw: ")
                .append(drawOdds).append(" (p=")
                .append(String.format("%.2f", 1.0 / drawOdds)).append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        return oddsToString();
    }
}
