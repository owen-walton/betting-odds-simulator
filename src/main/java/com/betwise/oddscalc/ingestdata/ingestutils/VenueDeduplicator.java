package com.betwise.oddscalc.ingestdata.ingestutils;

import com.betwise.oddscalc.entity.Venue;
import com.betwise.oddscalc.entity.VenueEditState;
import com.betwise.oddscalc.entity.VenueKey;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// class is made closeable as it is designed to only be used once per bulk insert
// this means the -1 ids in canonical map don't require updating after the insert
// and the recreation of the instance is not memory expensive because it is for bulk insert not single insert
public class VenueDeduplicator implements AutoCloseable {
    private Map<VenueKey, VenueEditState> canonicalVenues;
    private boolean isClosed;

    public VenueDeduplicator(Map<VenueKey, VenueEditState> canonicalVenues) {
        this.isClosed = false;
        this.canonicalVenues = canonicalVenues;
    }

    // handles all the deduplication rules/logic (anything that relies on other Venue entries determine)
    // if it is a unique venue return -1
    // otherwise return the trueID of the duplicate
    // and if new venue takes priority over the existing one, use editCanonicalMap() to change VenueKey but keep ID
    // Boolean value of true represents that the details of the venue have been changed and vice versa
    public int findAndUpdateDuplicateEntry(Venue venue) {
        checkClosed();

        VenueKey newKey = venue.getVenueKey();
        String newGround = newKey.groundName();

        if (newGround.isEmpty()) {
            canonicalVenues.put(newKey, new VenueEditState(-1, true));
            return -1;
        }

        char firstLetter = Character.toUpperCase(newGround.trim().charAt(0));

        for (Map.Entry<VenueKey, VenueEditState> entry : canonicalVenues.entrySet()) {
            String existingGround = entry.getKey().groundName();
            if (!existingGround.isEmpty() &&
                    Character.toUpperCase(existingGround.trim().charAt(0)) == firstLetter) {

                return entry.getValue().getId();
            }
        }

        canonicalVenues.put(newKey, new VenueEditState(-1, true));
        return -1;
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

    @Override
    public void close() throws Exception {
        isClosed = true;
        canonicalVenues = null; // free up memory
    }
}