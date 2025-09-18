package com.betwise.oddscalc.entity;

public enum Operation {
    PLUS("+"),
    MULTIPLY("*"),
    LOG_BASE_X("LogBaseX"),
    LOG_BASE_Y("LogBaseY"),
    X_POW_Y("X^Y"),
    Y_POW_X("Y^X");

    private final String dbValue;

    Operation(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static Operation fromDbValue(String dbValue) {
        for (Operation op : Operation.values()) {
            if (op.dbValue.equalsIgnoreCase(dbValue)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown Operation value: " + dbValue);
    }
}