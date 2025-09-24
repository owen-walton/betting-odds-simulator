package com.betwise.oddscalc.entity;

import java.time.LocalDateTime;

public class PredictionModel {
    private int modelId;
    private LocalDateTime modelDate;
    private float eValue;
    private float homeAdvantageMultiplier;

    public PredictionModel() {}

    public PredictionModel(int modelId,
                           LocalDateTime modelDate,
                           float eValue,
                           float homeAdvantageMultiplier) {
        this.modelId = modelId;
        this.modelDate = modelDate;
        this.eValue = eValue;
        this.homeAdvantageMultiplier = homeAdvantageMultiplier;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public LocalDateTime getModelDate() {
        return modelDate;
    }

    public void setModelDate(LocalDateTime modelDate) {
        this.modelDate = modelDate;
    }

    public float getEValue() {
        return eValue;
    }

    public void setEValue(float eValue) {
        this.eValue = eValue;
    }

    public float getHomeAdvantageMultiplier() {
        return homeAdvantageMultiplier;
    }

    public void setHomeAdvantageMultiplier(float homeAdvantageMultiplier) {
        this.homeAdvantageMultiplier = homeAdvantageMultiplier;
    }

    @Override
    public String toString() {
        return "PredictionModel{" +
                "modelId=" + modelId +
                ", modelDate=" + modelDate +
                ", eValue=" + eValue +
                ", homeAdvantageMultiplier=" + homeAdvantageMultiplier +
                '}';
    }
}
