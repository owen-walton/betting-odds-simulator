package com.betwise.oddscalc.entity;

public enum MatchFormat {
    TEST,
    ODI,
    T20;

    public static MatchFormat fromString(String value) {
        if (value == null) return null;

        switch (value.trim().toUpperCase()) {
            case "TEST" -> {
                return TEST;
            }
            case "ODI" -> {
                return ODI;
            }
            case "T20" -> {
                return T20;
            }
            default -> throw new IllegalArgumentException("Unknown match format: " + value);
        }
    }
}