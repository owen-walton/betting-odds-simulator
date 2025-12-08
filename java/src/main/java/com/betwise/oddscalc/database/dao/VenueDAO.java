package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.Venue;
import com.betwise.oddscalc.entity.VenueKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VenueDAO implements WriteDAO<Venue>, OneToManyMatches<Venue>, AutoCloseable {

    private DBConnection dbConnection;

    // Initialise connection
    public VenueDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException("Database connection failed");
        }
    }

    @Override
    public Set<Venue> getAll() {
        String sql = "SELECT VenueID, GroundName, City FROM Venue";
        Set<Venue> venues = new HashSet<>();

        try (Connection conn = dbConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int venueId = rs.getInt("VenueID");
                String groundName = rs.getString("GroundName");
                String city = rs.getString("City");

                venues.add(new Venue(venueId, new VenueKey(groundName, city)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return venues;
    }

    // takes in a list of Venue objects with filler/unassigned ids, e.g. -1
    // Uses the natural key of the object (groundName, city) to find the true ids from the db
    // Then build a new list of the correct Venue objects
    @Override
    public List<Venue> getIDsIntoObjects(List<Venue> venues) {
        List<Venue> newList = new ArrayList<>();
        if (venues.isEmpty()) return newList;

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement("SELECT VenueID, GroundName, City FROM Venue")) {
            List<Venue> dbVenues = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Venue dbVenue = new Venue();
                    dbVenue.setVenueID(rs.getInt("VenueID"));
                    dbVenue.setGroundName(rs.getString("GroundName"));
                    dbVenue.setCity(rs.getString("City"));
                    dbVenues.add(dbVenue);
                }
            }

            for (Venue input : venues) {
                for (Venue dbVenue : dbVenues) {
                    if (
                            input.getGroundName()
                                    .equals(dbVenue.getGroundName())
                            &&
                            input.getCity()
                                    .equals(dbVenue.getCity())
                    ) {
                        Venue matched = new Venue();
                        matched.setVenueID(dbVenue.getVenueID());
                        matched.setGroundName(input.getGroundName());
                        matched.setCity(input.getCity());
                        newList.add(matched);
                        break;
                    }
                }
            }

            return newList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean insert(Venue venue) {
        String sql = "INSERT INTO Venue (GroundName, City) VALUES (?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, venue.getGroundName());
            statement.setString(2, venue.getCity());

            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // receives a list of Venue objects and remove all duplicates (keep first one then remove rest),
    // then for the Venues still in list, search db (by natural key) for each one,
    // if not found add it to a new list then return the new list
    @Override
    public List<Venue> removeExisting(List<Venue> venues) {
        if (venues.isEmpty()) {
            return new ArrayList<>();
        }

        // remove all duplicate venue appearances in list
        Set<VenueKey> uniqueKeys = new HashSet<>();
        List<Venue> uniqueVenues = new ArrayList<>();
        for (Venue venue : venues) {
            VenueKey key = new VenueKey(venue.getGroundName(), venue.getCity()); // or country, depending on your key
            if (!uniqueKeys.contains(key)) {
                uniqueKeys.add(key);
                uniqueVenues.add(venue);
            }
        }
        venues = uniqueVenues;

        // remove all that appear in db
        // prepare sql
        StringBuilder sb = new StringBuilder("SELECT GroundName, City FROM Venue WHERE (GroundName, City) IN (");
        for (int i = 0; i < venues.size(); i++) {
            sb.append("(?, ?)");
            if (i < venues.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        String sql = sb.toString();

        // execute sql
        Set<VenueKey> existingVenueKeys = new HashSet<>();
        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            for (Venue venue : venues) {
                ps.setString(i++, venue.getGroundName());
                ps.setString(i++, venue.getCity()); // or country, depending on your key
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingVenueKeys.add(new VenueKey(rs.getString("GroundName"), rs.getString("City")));
                }
            }

            List<Venue> newVenues = new ArrayList<>();
            for (Venue venue : venues) {
                VenueKey key = new VenueKey(venue.getGroundName(), venue.getCity());
                if (!existingVenueKeys.contains(key)) {
                    newVenues.add(venue);
                }
            }
            return newVenues;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // insert a List of Venue objects to DB
    // adds each singular object to the same batch before executing the entire batch to reduce querying
    // this increases efficiency and reduces points of failure
    @Override
    public void bulkInsertIfNotExists(List<Venue> venues) {
        venues = removeExisting(venues);

        if (venues.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO Venue (GroundName, City) " +
                "VALUES (?, ?)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (Venue venue : venues) {
                statement.setString(1, venue.getGroundName());
                statement.setString(2, (venue.getCity() == null ? "" : venue.getCity()));
                statement.addBatch();
            }

            statement.executeBatch();
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // if venueID is -1 then it will insert the venue
    // if the venueID is already a VenueID in the table it will update the details and keep VenueID
    // this means that if a more canonical format of an existing venue key is found in ingestion,
    // then it will update the db record
    public void bulkInsertAndUpdate(List<Venue> venues) {
        venues = removeExisting(venues);

        if (venues.isEmpty()) {
            return;
        }

        String sql = """
        INSERT INTO Venue (VenueID, GroundName, City)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE
            GroundName = VALUES(GroundName),
            City = VALUES(City)
        """;

        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Venue venue : venues) {
                // if venueID == -1, insert null so the primary key auto-increment triggers
                if (venue.getVenueID() == -1) {
                    ps.setNull(1, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(1, venue.getVenueID());
                }
                ps.setString(2, venue.getGroundName());
                ps.setString(3, venue.getCity() == null ? "" : venue.getCity());
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() {
        dbConnection.disconnect();
    }
}