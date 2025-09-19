package com.betwise.oddscalc.entity;

import java.time.LocalDateTime;

public class PredictionModel {
    private int modelId;
    private LocalDateTime modelDate;
    private float eValue;
    private float eloGain;
    private float tossWinnerEloGainMultiplier;
    private float tossWinnerMultiplier;
    private float winMarginMultiplier;
    private float homeAdvantageMultiplier;

    public PredictionModel() {}

    public PredictionModel(int modelId,
                           LocalDateTime modelDate,
                           float eValue,
                           float eloGain,
                           float tossWinnerEloGainMultiplier,
                           float tossWinnerMultiplier,
                           float winMarginMultiplier,
                           float homeAdvantageMultiplier) {
        this.modelId = modelId;
        this.modelDate = modelDate;
        this.eValue = eValue;
        this.eloGain = eloGain;
        this.tossWinnerEloGainMultiplier = tossWinnerEloGainMultiplier;
        this.tossWinnerMultiplier = tossWinnerMultiplier;
        this.winMarginMultiplier = winMarginMultiplier;
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

    public float geteValue() {
        return eValue;
    }

    public void seteValue(float eValue) {
        this.eValue = eValue;
    }

    public float getEloGain() {
        return eloGain;
    }

    public void setEloGain(float eloGain) {
        this.eloGain = eloGain;
    }

    public float getTossWinnerEloGainMultiplier() {
        return tossWinnerEloGainMultiplier;
    }

    public void setTossWinnerEloGainMultiplier(float tossWinnerEloGainMultiplier) {
        this.tossWinnerEloGainMultiplier = tossWinnerEloGainMultiplier;
    }

    public float getTossWinnerMultiplier() {
        return tossWinnerMultiplier;
    }

    public void setTossWinnerMultiplier(float tossWinnerMultiplier) {
        this.tossWinnerMultiplier = tossWinnerMultiplier;
    }

    public float getWinMarginMultiplier() {
        return winMarginMultiplier;
    }

    public void setWinMarginMultiplier(float winMarginMultiplier) {
        this.winMarginMultiplier = winMarginMultiplier;
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
                ", eloGain=" + eloGain +
                ", tossWinnerEloGainMultiplier=" + tossWinnerEloGainMultiplier +
                ", tossWinnerMultiplier=" + tossWinnerMultiplier +
                ", winMarginMultiplier=" + winMarginMultiplier +
                ", homeAdvantageMultiplier=" + homeAdvantageMultiplier +
                '}';
    }
}