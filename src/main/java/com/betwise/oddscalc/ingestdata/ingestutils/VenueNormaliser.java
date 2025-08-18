package com.betwise.oddscalc.ingestdata.ingestutils;

import com.betwise.oddscalc.entity.VenueKey;

public class VenueNormaliser {
    public VenueNormaliser() {
    }

    public VenueKey normaliseVenueKey(VenueKey venueKey) {
        return new VenueKey(normalise(venueKey.groundName()), normalise(venueKey.city()));
    }

    public String normalise(String input) {
        if (input == null) {
            return "";
        }

        String result = input.trim();
        result = result.replaceAll("-", " ");

        // remove punctuation
        StringBuilder sb = new StringBuilder();
        for (char c : result.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                continue; // skip punctuation
            }
            sb.append(c);
        }
        result = sb.toString();

        // convert string of multiple spaces to 1 space
        result = result.replaceAll("\\s+", " ");

        // convert to title case
        result = result.toLowerCase();
        StringBuilder titleCase = new StringBuilder();
        boolean nextUpper = true;
        for (char c : result.toCharArray()) {
            if (Character.isWhitespace(c)) {
                titleCase.append(c);
                nextUpper = true;
            } else if (nextUpper) {
                titleCase.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                titleCase.append(c);
            }
        }
        result = titleCase.toString();

        return result;
    }
}