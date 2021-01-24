package kerbals;

import missions.Mission;

public class Kerbal {

    // Basic information
    private final String name;
    private final boolean male;
    private final Role role;

    // Specific details
    private final FlightLog log;
    private boolean onDuty;
    private int level;


    public Kerbal(String name, boolean isMale, Role role, int level) {
        this.name = name;
        this.male = isMale;
        this.role = role;
        this.level = level;
        this.log = new FlightLog(this);
        this.onDuty = false;
    }

    public Role getRole() {
        return role;
    }

    public boolean isMale() {
        return male;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return !onDuty;
    }

    public void setOnDuty(boolean onDuty) {
        this.onDuty = onDuty;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        if (level == 5) return;
        level++;
    }

    public void missionComplete(Mission m) {
        log.addEntry(m);

    }
}
