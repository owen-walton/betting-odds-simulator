package com.betwise.oddscalc.entity;

public record TuningParams(float eValue, float kFactor, float decay, float startingELO,
                           float homeAdvMultiplier, float tossAdvMultiplier, float winMarginMultiplier) {
    /**
     * stores naive values that will be used for optimising against
     * https://www.reddit.com/r/Cricket/comments/1hddsht/an_updated_elo_rating_system_for_test_cricket/
     * Link above suggests 25 is a strong naive value for K however will require tuning based on starting elo etc.
     */
    public TuningParams() {
        this(400, 25, 0.01f, 1500, 10, 5, 1);
    }
}