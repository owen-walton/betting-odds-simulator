package com.betwise.oddscalc.ingestdata.ingestutils;

import com.betwise.oddscalc.entity.Venue;
import com.betwise.oddscalc.entity.VenueEditState;
import com.betwise.oddscalc.entity.VenueKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// class is made closeable as it is designed to only be used once per bulk insert
// this means the -1 ids in canonical map don't require updating after the insert
// and the recreation of the instance is not memory expensive because it is for bulk insert not single insert
public class VenueDeduplicator implements AutoCloseable {
    private Map<VenueKey, VenueEditState> canonicalVenues;
    private boolean isClosed;
    private Map<String, String> groundAliasMap;
    private Map<String, String> cityAliasMap;

    public VenueDeduplicator(Map<VenueKey, VenueEditState> canonicalVenues) {
        this.isClosed = false;
        this.canonicalVenues = canonicalVenues;
        createAliasMaps();
    }

    // handles all the deduplication rules/logic (anything that relies on other Venue entries determine)
    // if it is a unique venue return -1 and put in canonical map with a new VenueEditState(-1, true)
    // otherwise return the trueID of the duplicate
    // and if new venue takes priority over the existing one, use editCanonicalMap() to change VenueKey but keep ID
    // Boolean value of true represents that the details of the venue have been changed and vice versa
    public int findAndUpdateDuplicateEntry(Venue venue) {
        VenueKey newKey = aliasCheck(venue.getVenueKey());
        VenueKey matchKey = null;
        VenueEditState matchState = null;

        String groundName = newKey.groundName();
        String city = newKey.city();

        // find the best matching venue in canonical list
        // (if no valid matches then existingMatchKey and existingMatchState = null)
        for (Map.Entry<VenueKey, VenueEditState> entry : canonicalVenues.entrySet()) {
            VenueKey existingKey = entry.getKey();
            VenueEditState existingState = entry.getValue();

            if (isSameVenue(groundName, city, existingKey.groundName(), existingKey.city())) {
                matchKey = existingKey;
                matchState = existingState;
                break; // a venue being checked can only match 1 from the canonical list
            }
        }

        // if no match, add new canonical entry and return -1
        if (matchKey == null) {
            canonicalVenues.put(newKey, new VenueEditState(-1, true));
            return -1;
        }

        // if match found, decide if new venue takes priority
        if (shouldReplace(newKey, matchKey)) {
            // keep id, update key
            editCanonicalVenue(matchState.getId(), newKey);
        }

        return matchState.getId();
    }

    private boolean isSameVenue(String name, String city, String exName, String exCity) {
        // Normalise in place
        name   = normaliseForComparison(stripGroundName(name));
        city   = normaliseForComparison(city);
        exName = normaliseForComparison(stripGroundName(exName));
        exCity = normaliseForComparison(exCity);

        if (name.equals(exName)) {
            return true;
        }

        String base     = nameWithoutNumericSuffix(name);
        String exBase   = nameWithoutNumericSuffix(exName);
        boolean hasSuf  = hasNumericSuffix(name);
        boolean exHasSuf= hasNumericSuffix(exName);
        if (base.equals(exBase)) {
            if (hasSuf || exHasSuf) {
                // both have suffix, must match exactly
                if (hasSuf && exHasSuf) {
                    String suf  = name.substring(base.length());
                    String exSuf = exName.substring(exBase.length());

                    if (suf.equals(exSuf)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                else {
                    // one has a number, the other doesn’t → distinct
                    return false;
                }
            }
        }

        return false;
    }

    private boolean shouldReplace(VenueKey newKey, VenueKey oldKey) {
        String newCity = newKey.city();
        String oldCity = oldKey.city();
        String newName = newKey.groundName();
        String oldName = oldKey.groundName();

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
        // don't add oval due to potential for false duplicates
        final String[] GENERIC_WORDS = { "ground", "stadium", "sports", "international", "field", "park", "club",
                "academy", "cricket", "school", "college", "institute", "university", "national", "state",
                "recreation", "complex", "arena", "centre", "center", "the", "association" };
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
        return new VenueKey(key.groundName().replaceAll("\\s+", "").toLowerCase(),
                key.city().replaceAll("\\s+", "").toLowerCase());
    }

    private boolean hasNumericSuffix(String s) {
        return s.matches(".*\\d+$");
    }

    private String nameWithoutNumericSuffix(String s) {
        return s.replaceAll("\\d+$", "");
    }

    public Set<Venue> getEditedAndNewVenues() {
        Set<Venue> changedVenues = new HashSet<>();
        for (Map.Entry<VenueKey, VenueEditState> entry : getCanonicalVenues().entrySet()) {
            if (entry.getValue().isEdited()) {
                changedVenues.add(new Venue(entry.getValue().getId(), entry.getKey()));
            }
        }
        return changedVenues;
    }

    /*
    * Return value of this function is a set of venues which contains:
    * - The ground name and city used as a natural key in CricketMatchDataSchema
    * - The true id in the db that relates to the natural key
    * - natural key is not updated in accordance with the db because the return value is only used to replace the
    *   foreign keys in CricketMatchDataSchema with true keys and no updating is then required as the Venue part
    *   of the schema is then not used again
    * - True id of unique venues cannot be retrieved as canonicalList not added to db yet so they have venueID of -1 so
    *   service layer knows to call VenueDAO.getIDsIntoObjects() for those venues specifically once added to db
     */
    public Set<Venue> updateCanonicalList(Set<Venue> venuesToAdd) {
        Set<Venue> trueIDvenues = new HashSet<>();
        for (Venue v : venuesToAdd) {
            int trueID = findAndUpdateDuplicateEntry(v);

            // if the venue is not a duplicate anywhere add the venue to the canonical list and -1 trueID
            if (trueID == -1) {
                trueIDvenues.add(v); // + getIDsIntoObjects
            } else {
                // set trueID to the one from its duplicate counterpart
                v.setVenueID(trueID);
                trueIDvenues.add(v);
            }
        }
        return trueIDvenues;
    }

    public Map<VenueKey, VenueEditState> getCanonicalVenues() {
        return canonicalVenues;
    }

    public void editCanonicalVenue(int id, VenueKey newVenueKey) {
        VenueKey oldVenueKey = null;
        for (Map.Entry<VenueKey, VenueEditState> v : this.canonicalVenues.entrySet()) {
            int currentID = v.getValue().getId();
            if (currentID == id) {
                oldVenueKey = v.getKey();
                break;
            }
        }
        if (oldVenueKey != null) {
            this.canonicalVenues.remove(oldVenueKey);
            this.canonicalVenues.put(newVenueKey, new VenueEditState(id, true));
        }

    }

    public void checkClosed() {
        if (isClosed) {
            throw new RuntimeException("Venue duplicator is closed");
        }
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
        addGroundAlias("Dubai Sports City Cricket Stadium", "Dubai International Cricket Stadium");

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

    private VenueKey aliasCheck(VenueKey key) {
        String ground = groundAliasMap.getOrDefault(key.groundName().toLowerCase(), key.groundName());
        String city   = cityAliasMap.getOrDefault(key.city().toLowerCase(), key.city());
        return new VenueKey(ground, city);
    }

    @Override
    public void close() throws Exception {
        isClosed = true;
        canonicalVenues = null; // free up memory
        cityAliasMap = null;
        groundAliasMap = null;
    }
}