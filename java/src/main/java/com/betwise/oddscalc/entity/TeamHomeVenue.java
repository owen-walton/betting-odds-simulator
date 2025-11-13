/**
 * @author Owen Walton
 * Java Record container that mirrors a singular record in the TeamHomeVenue database table
 * Doesn't use any complex logic or need to be mutable so doesn't need to be a class
 */

package com.betwise.oddscalc.entity;

public record TeamHomeVenue(int teamHomeVenueID, int teamID, int venueID) { }
