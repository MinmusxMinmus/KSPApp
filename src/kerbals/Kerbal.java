package kerbals;

import missions.Mission;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Kerbal {

    // Basic information
    private final String name;
    private final boolean male;
    private final Role role;

    // Specific details
    private final List<FlightLog> log;
    private boolean available;
    private int level;

    private String notes;


    public Kerbal(String name, boolean isMale, Role role, int level) {
        this.name = name;
        this.male = isMale;
        this.role = role;
        this.level = level;
        this.log = new LinkedList<>();
        this.available = false;
    }


    public String getName() {
        return name;
    }

    public boolean isMale() {
        return male;
    }

    public Role getRole() {
        return role;
    }

    public List<FlightLog> getLog() {
        return Collections.unmodifiableList(log);
    }

    public boolean isAvailable() {
        return !available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        if (level == 5) return;
        level++;
    }

    public void missionComplete(Mission m, int expGained) {
        log.add(new FlightLog(m, expGained));
    }

    public void setKIA(Mission m) {
        log.add(new FlightLog(m, 0));
    }
}
