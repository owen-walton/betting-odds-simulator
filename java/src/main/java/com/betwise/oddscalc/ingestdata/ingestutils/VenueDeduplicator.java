/**
 * @author Owen Walton
 * Stores the rules and logic behind determining if 2 venues named differently are the same place
 * This is necessary due to inconsistencies across the data sources
 */

package com.betwise.oddscalc.ingestdata.ingestutils;

import com.betwise.oddscalc.entity.VenueKey;

import java.util.HashMap;
import java.util.Map;

public class VenueDeduplicator implements AutoCloseable {
    private boolean isClosed;
    private Map<String, String> groundAliasMap;
    private Map<String, String> cityAliasMap;

    public VenueDeduplicator() {
        this.isClosed = false;
        createAliasMaps();
    }

    // takes in a name,city and an existing name,city
    // returns true if they are determined to be the same place, false if not
    //
    // RULES:
    // normalise both venues and applies alias, strip generic words from ground name and normalise again for comparison
    // if the cleaned names match exactly then same venue
    // otherwise remove numeric suffixes (e.g. "ground2" → "ground"), if base names don't match then not same venue
    // if base names match, if one has a numeric suffix and the other doesn't then not same venue,
    // if both have numeric suffixes they must match
    public boolean isSameVenue(String name, String city, String exName, String exCity) {
        // Create VenueKey objects
        VenueKey key1 = aliasCheck(new VenueKey(name, city));
        VenueKey key2 = aliasCheck(new VenueKey(exName, exCity));

        // Normalise names and cities for comparison
        String name1 = normaliseForComparison(stripGroundName(key1.groundName()));
        String city1 = normaliseForComparison(key1.city());
        String name2 = normaliseForComparison(stripGroundName(key2.groundName()));
        String city2 = normaliseForComparison(key2.city());

        if (name1.equals(name2)) {
            return true;
        }

        String base1 = nameWithoutNumericSuffix(name1);
        String base2 = nameWithoutNumericSuffix(name2);
        boolean hasSuf1 = hasNumericSuffix(name1);
        boolean hasSuf2 = hasNumericSuffix(name2);

        if (base1.equals(base2)) {
            if (hasSuf1 || hasSuf2) {
                if (hasSuf1 && hasSuf2) {
                    String suf1 = name1.substring(base1.length());
                    String suf2 = name2.substring(base2.length());
                    return suf1.equals(suf2);
                } else {
                    return false; // one has suffix, other doesn't
                }
            }
        }

        return false;
    }

    // takes in a new venue key and an old venue key
    // returns true if the new entry should replace the old one, false if not
    //
    // RULES:
    // apply alias rules to the new key, if alias changes it then prefer the new key
    // apply alias rules to the old key, if alias changes old but not new then keep old
    // prefer the entry with a non-empty city over one with an empty city
    // prefer the name that includes a numeric suffix if the old one doesn't
    // prefer the entry with the longer (more specific) city name
    // otherwise do not replace
    public boolean shouldReplace(VenueKey newKey, VenueKey oldKey) {
        // Apply alias normalization
        VenueKey normNew = aliasCheck(newKey);
        if (!normNew.equals(newKey)) return true;
        VenueKey normOld = aliasCheck(oldKey);
        if (!normOld.equals(oldKey)) return false;

        String newCity = normNew.city();
        String oldCity = normOld.city();
        String newName = normNew.groundName();
        String oldName = normOld.groundName();

        // prefer non-empty city
        if (oldCity.isEmpty() && !newCity.isEmpty()) return true;

        // prefer name with numeric suffix if old doesn't have it
        if (!hasNumericSuffix(oldName) && hasNumericSuffix(newName)) return true;

        // prefer longer city name (more specific)
        if (newCity.length() > oldCity.length()) return true;

        // otherwise don't replace
        return false;
    }

    private String stripGroundName(String groundName) {
        // don't add "oval" due to potential for false duplicates
        final String[] GENERIC_WORDS = {"ground", "stadium", "sports", "international", "field", "park", "club",
                "academy", "cricket", "school", "college", "institute", "university", "national", "state",
                "recreation", "complex", "arena", "centre", "center", "the", "association"};
        for (String word : GENERIC_WORDS) {
            // only replaces if the word in array is a full word in the string (e.g 'statement' isn't reduced to 'ment')
            groundName = groundName.toLowerCase().replaceAll("(?i)\\b" + word + "\\b", "");
        }
        return groundName;
    }

    private String normaliseForComparison(String s) {
        // punctuation, etc already removed before given to VenueDeduplicator
        return s.replaceAll("\\s+", "").toLowerCase();
    }

    private VenueKey normaliseForComparison(VenueKey key) {
        // punctuation, etc already removed before given to VenueDeduplicator
        return new VenueKey(normaliseForComparison(key.groundName()),
                normaliseForComparison(key.city()));
    }

    private boolean hasNumericSuffix(String s) {
        return s.matches(".*\\d+$");
    }

    private String nameWithoutNumericSuffix(String s) {
        return s.replaceAll("\\d+$", "");
    }

    private void createAliasMaps() {
        groundAliasMap = new HashMap<>();
        cityAliasMap = new HashMap<>();

        addGroundAlias("Sardar Patel Gujarat Stadium", "Narendra Modi Stadium");
        addGroundAlias("Sardar Patel Stadium", "Narendra Modi Stadium");
        addGroundAlias("Beausejour Stadium", "Daren Sammy National Cricket Stadium");
        addGroundAlias("Darren Sammy National Cricket Stadium", "Daren Sammy National Cricket Stadium");
        addGroundAlias("Feroz Shah Kotla", "Arun Jaitley Stadium");
        addGroundAlias("P Sara Oval", "R Premadasa Stadium");
        addGroundAlias("P Saravanamuttu Stadium", "R Premadasa Stadium");
        addGroundAlias("Zohur Ahmed Chowdhury Stadium", "Zahur Ahmed Chowdhury Stadium");
        addGroundAlias("Chittagong Divisional Stadium", "Zahur Ahmed Chowdhury Stadium");
        addGroundAlias("Ma Aziz Stadium", "Zahur Ahmed Chowdhury Stadium");
        addGroundAlias("New Wanderers Stadium", "Wanderers Stadium");
        addGroundAlias("Vidarbha Ca Ground", "Vidarbha Cricket Association Stadium");
        addGroundAlias("Punjab Cricket Association Is Bindra Stadium", "Punjab Cricket Association Stadium ");
        addGroundAlias("Gahanga International Cricket Stadium Rwanda", "Gahanga International Cricket Stadium");
        addGroundAlias("Boland Park", "Boland Bank Park");
        addGroundAlias("Dubai International Cricket Stadium", "Dubai Sports City Cricket Stadium");

        addCityAlias("Gros Islet", "Gros Islet");
        addCityAlias("Chittagong", "Chattogram");
        addCityAlias("Ahmedabad", "Ahmedabad");
        addCityAlias("Delhi", "Delhi");
        addCityAlias("Colombo", "Colombo");
        addCityAlias("Johannesburg", "Johannesburg");
    }

    private void addGroundAlias(String alias, String canonical) {
        groundAliasMap.put(alias.toLowerCase(), canonical);
    }

    private void addCityAlias(String alias, String canonical) {
        cityAliasMap.put(alias.toLowerCase(), canonical);
    }

    // returns an alias clean VenueKey object
    // if wanting a true/false value you can do: vk.equals(aliasCheck(vk)), returns true if no alias, vice versa
    private VenueKey aliasCheck(VenueKey key) {
        String ground = groundAliasMap.getOrDefault(key.groundName().toLowerCase(), key.groundName());
        String city = cityAliasMap.getOrDefault(key.city().toLowerCase(), key.city());
        return new VenueKey(ground, city);
    }

    @Override
    public void close() throws Exception {
        isClosed = true;
        cityAliasMap = null;
        groundAliasMap = null;
    }
}