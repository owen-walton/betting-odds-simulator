package com.betwise.oddscalc.ingestdata;

import com.betwise.oddscalc.entity.VenueKey;

import java.util.Map;
import java.util.Set;

public class VenueNormaliser {
    private Map<String, Set<String>> venueAliasMap;

    public VenueNormaliser(Map<String, Set<String>> venueAliasMap) {
        this.venueAliasMap = venueAliasMap;
    }

    public VenueKey normaliseVenueKey(VenueKey venueKey) {
        VenueKey newVenueKey = new VenueKey(normalise(venueKey.groundName()), normalise(venueKey.city()));

        /*
         * use alias map here
         */

        return newVenueKey;
    }

    public String normalise(String input) {
        if (input == null) {
            return null;
        }

        String result = input.trim();

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