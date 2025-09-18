package com.betwise.oddscalc.entity;

public class FactorApplication {
    private int applyOrder;
    private AppliesTo appliesTo;       // ENUM('E', 'K')
    private String factorName;
    private float factorValue;
    private Operation operation;       // ENUM('+', '*', 'LogBaseX', 'LogBaseY', 'X^Y', 'Y^X')

    public FactorApplication() {}

    public FactorApplication(int applyOrder, AppliesTo appliesTo, String factorName, float factorValue, Operation operation) {
        this.applyOrder = applyOrder;
        this.appliesTo = appliesTo;
        this.factorName = factorName;
        this.factorValue = factorValue;
        this.operation = operation;
    }

    public int getApplyOrder() {
        return applyOrder;
    }

    public void setApplyOrder(int applyOrder) {
        this.applyOrder = applyOrder;
    }

    public AppliesTo getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(AppliesTo appliesTo) {
        this.appliesTo = appliesTo;
    }

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    public float getFactorValue() {
        return factorValue;
    }

    public void setFactorValue(float factorValue) {
        this.factorValue = factorValue;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "FactorApplication{" +
                "applyOrder=" + applyOrder +
                ", appliesTo=" + (appliesTo != null ? appliesTo.name() : "null") +
                ", factorID=" + factorName +
                ", factorValue=" + factorValue +
                ", operation=" + (operation != null ? operation.name() : "null") +
                '}';
    }
}
