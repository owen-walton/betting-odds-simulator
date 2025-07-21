package com.betwise.oddscalc.entity;

public enum TossDecision {
    BAT, FIELD;

    @Override
    // toString where "_" is a space and each word is title case
    public String toString() {
        String[] words = name().toLowerCase().split("_");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            str.append(words[i].charAt(0));
            if (words[i].length() > 1) {
                str.append(words[i].substring(1));
            }
            // don't add space at end when no words left
            if (i != words.length - 1) {
                str.append(" ");
            }

        }
        return str.toString();
    }
}
