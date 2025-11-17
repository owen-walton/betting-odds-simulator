/**
 * @author Owen Walton
 * Helper class to normalise input for equal comparison, whilst keeping it presentable (i.e. title case)
 */

package com.betwise.oddscalc.ingestdata.ingestutils;

import com.betwise.oddscalc.entity.VenueKey;

public final class Normaliser {
    private Normaliser() {
    }

    // wrapper to normalise both fields in a venue key
    public static VenueKey normaliseVenueKey(VenueKey venueKey) {
        return new VenueKey(normalise(venueKey.groundName()), normalise(venueKey.city()));
    }

    // returns the input with punctuation removed, spaces normalised, and words title-cased.
    public static String normalise(String input) {
        if (input == null) {
            return "";
        }

        String result = input.trim();
        // convert unicode apostrophe to normal apostrophe
        result = result.replaceAll("u0027", "'");
        // convert HTML right-apostrophe to normal apostrophe
        result = result.replaceAll("&rsquo;", "'");
        // replace hyphen with space so there are no hyphens but words aren't merged unwantedly
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