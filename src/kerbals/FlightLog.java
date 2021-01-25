package kerbals;

import missions.Mission;

public class FlightLog {

    private final Mission mission;
    private final float expGained;

    private String notes;

    public FlightLog(Mission mission, float expGained) {
        this.mission = mission;
        this.expGained = expGained;
    }

    public Mission getMission() {
        return mission;
    }

    public float getExpGained() {
        return expGained;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
