package kerbals;

import missions.Mission;
import other.KSPDate;
import other.Listable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Kerbal implements Listable {

    // Basic information
    /**
     * The kerbal's name. The surname is not included.
     */
    private final String name;
    /**
     * True if male, false if female
     */
    private final boolean male;
    /**
     * The kerbal's job position.
     */
    private final Role role;
    /**
     * The mission where the kerbal was rescued in. A null value indicates a hired astronaut.
     */
    private Mission origin;
    private final KSPDate hiringDate;

    // Specific details
    /**
     * The kerbal's flight log. Contains information regarding all of the past missions.
     */
    private final List<FlightLog> log;
    /**
     * Indicates the availability of the kerbal for a new mission.
     */
    private boolean available;
    /**
     * Honorable mentions that speak to the kerbal's achievements.
     */
    private final List<String> condecorations;
    /**
     * The kerbal's total experience
     */
    private float experience;
    private String notes;


    /** Generates a new kerbal.
     * @param name The kerbal's name.
     * @param isMale The kerbal's gender.
     * @param role The kerbal's job.
     * @param hiringDate The date the kerbal was hired / rescued.
     */
    public Kerbal(String name, boolean isMale, Role role, KSPDate hiringDate) {
        this.name = name;
        this.male = isMale;
        this.role = role;
        this.hiringDate = hiringDate;

        this.log = new LinkedList<>();
        this.available = true;
        this.condecorations = new LinkedList<>();
        this.experience = 0;
    }

    /** Sets the mission where the kerbal was rescued. Cannot be changed once set.
     * @param origin The mission where the kerbal originated from.
     */
    public void setOrigin(Mission origin) {
        if (this.origin != null) return;
        this.origin = origin;
    }

    /** Executed whenever a kerbal is succesfully recovered back from a mission.
     * @param mission The mission the kerbal took part in.
     */
    public void missionComplete(Mission mission) {
        log.add(new FlightLog(mission, mission.getExperienceGained(this)));
        available = true;
        experience += mission.getExperienceGained(this);
    }

    /** Executed whenever a kerbal unfortunately goes KIA.
     * @param mission The mission the kerbal took part of.
     */
    public void KIA(Mission mission) {
        log.add(new FlightLog(mission, 0));
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

    public Mission getOrigin() {
        return origin;
    }

    public KSPDate getHiringDate() {
        return hiringDate;
    }

    public List<FlightLog> getLog() {
        return Collections.unmodifiableList(log);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getLevel() {
        if (experience > 64.0) return 5;
        if (experience > 32.0) return 4;
        if (experience > 16.0) return 3;
        if (experience > 8.0) return 2;
        if (experience > 2.0) return 1;
        return 0;
    }

    public List<String> getCondecorations() {
        return condecorations;
    }

    public float getExperience() {
        return experience;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getTextRepresentation() {
        return name + " Kerman (" + role.toString() + (available ? ", available)" : ", deployed)");
    }
}
