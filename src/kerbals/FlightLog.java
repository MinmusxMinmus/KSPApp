package kerbals;

import java.util.StringJoiner;

public class FlightLog {

    private static final String DELIMITER = ":fl:";

    private final String missionName;
    private final float expGained;

    private String notes;

    public FlightLog(String mission, float expGained) {
        this.missionName = mission;
        this.expGained = expGained;
    }

    public static FlightLog fromString(String s) {
        String[] split = s.split(DELIMITER);
        if (split.length != 2) return null;
        return new FlightLog(split[0], Float.parseFloat(split[1]));
    }

    public static String toString(FlightLog log) {
        return new StringJoiner(DELIMITER)
                .add(log.missionName)
                .add(Float.toString(log.expGained))
                .toString();
    }

    public String getMissionName() {
        return missionName;
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
