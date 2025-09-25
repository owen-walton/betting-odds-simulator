package com.betwise.oddscalc.entity;

public class VenueEditState {
    private int id;
    private boolean edited;

    public VenueEditState(int id, boolean edited) {
        this.id = id;
        this.edited = edited;
    }

    public int getId() {
        return id;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VenueEditState)) return false;
        VenueEditState that = (VenueEditState) o;
        return id == that.id &&
                edited == that.edited;
    }

    @Override
    public int hashCode() {
        // Combines id and edited into a single hash
        int result = Integer.hashCode(id);
        // multiplied by 31 to reduce the overlapping (common java practice)
        result = 31 * result + Boolean.hashCode(edited);
        return result;
    }
}