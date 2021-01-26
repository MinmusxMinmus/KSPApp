package kerbals;

import missions.Mission;
import other.KSPDate;
import other.KSPObject;

import java.util.*;
import java.util.stream.Collectors;

public class Kerbal extends KSPObject {

    public static final String DELIMITER = ":k:";
    public static final int ENCODE_FIELD_AMOUNT = 11; // ALWAYS ACCOUNT FOR DESCRIPTION

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
     * Badass flag
     */
    private boolean badass;
    /**
     * The kerbal's job position.
     */
    private final Job job;
    /**
     * The mission where the kerbal was rescued in.
     */
    private final String origin;
    /**
     * The date in which the kerbal joined the space center crew.
     */
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


    /** Generates a new kerbal.
     * @param name The kerbal's name.
     * @param isMale The kerbal's gender.
     * @param job The kerbal's job.
     * @param hiringDate The date the kerbal was hired / rescued.
     */
    public Kerbal(String name,
                  boolean isMale,
                  Job job,
                  String origin,
                  KSPDate hiringDate) {
        this.name = name;
        this.male = isMale;
        this.badass = false;
        this.job = job;
        this.origin = origin;
        this.hiringDate = hiringDate;

        this.log = new LinkedList<>();
        this.available = true;
        this.condecorations = new LinkedList<>();
        this.experience = 0;

    }

    /** Generates a kerbal from a list of fields stores in persistence.
     * @param fields List of fields
     */
    public Kerbal(List<String> fields) {
        this.name = fields.get(1);
        this.male = Boolean.getBoolean(fields.get(2));
        this.badass = Boolean.getBoolean(fields.get(3));
        this.job = Job.fromString(fields.get(4));
        this.origin = fields.get(5);
        this.hiringDate = KSPDate.fromString(fields.get(6));
        this.log = flightLogFromString(fields.get(7));
        this.available = fields.get(8).equals("true");
        this.condecorations = condecorationsFromString(fields.get(9));
        this.experience = Float.parseFloat(fields.get(10));
        setDescription((fields.get(0)));
    }

    // ORDER: DESC, NAME, MALE, BADASS, JOB, ORIGIN, DATE, AVAILABLE, FLOG, CONDEC, EXP

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = super.toStorableCollection();

        ret.add(name);
        ret.add(Boolean.toString(male));
        ret.add(Boolean.toString(badass));
        ret.add(job.toString());
        ret.add(origin);
        ret.add(hiringDate.toStorableString());
        ret.add(flightLogToString());
        ret.add(available ? "true" : "false");
        ret.add(condecorationsToString());
        ret.add(Float.toString(experience));

        return ret;
    }

    /** Executed whenever a kerbal is succesfully recovered back from a mission.
     * @param mission The mission the kerbal took part in.
     */
    public void missionComplete(Mission mission) {
        log.add(new FlightLog(mission.getName(), mission.getExperienceGained(this)));
        available = true;
        experience += mission.getExperienceGained(this);
    }

    /** Executed whenever a kerbal unfortunately goes KIA.
     * @param mission The mission the kerbal took part of.
     */
    public void KIA(Mission mission) {
        log.add(new FlightLog(mission.getName(), 0));
    }

    public String getName() {
        return name;
    }

    public boolean isMale() {
        return male;
    }

    public Job getRole() {
        return job;
    }

    public String getOrigin() {
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

    @Override
    public String getTextRepresentation() {
        return name + " Kerman (" + job.toString() + (available ? ", available)" : ", deployed)");
    }

    @Override
    public int getFieldCount() {
        return 6;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Name";
            case 1 -> "Job";
            case 2 -> "Gender";
            case 3 -> "Level";
            case 4 ->"Date of recruitment";
            case 5 -> "Deployed";
            default -> null;
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> name;
            case 1 -> job.toString();
            case 2 -> male ? "Male" : "Female";
            case 3 -> Integer.toString(getLevel());
            case 4 -> hiringDate.getTextRepresentation();
            case 5 -> Boolean.toString(!available);
            default -> null;
        };
    }

    public boolean isBadass() {
        return badass;
    }

    public void setBadass(boolean badass) {
        this.badass = badass;
    }

    private String flightLogToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (FlightLog log : log) joiner.add(FlightLog.toString(log));
        return joiner.toString().equals("") ? "(none)" : joiner.toString();
    }

    private static List<FlightLog> flightLogFromString(String field) {
        if (field.equals("(none)")) return new LinkedList<>();
        return Arrays.stream(field.split(DELIMITER)).map(FlightLog::fromString).collect(Collectors.toList());
    }

    private String condecorationsToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (String c : condecorations) joiner.add(c);
        return joiner.toString().equals("") ? "(none)" : joiner.toString();
    }

    private static List<String> condecorationsFromString(String fields) {
        if (fields.equals("(none)")) return new LinkedList<>();
        return Arrays.stream(fields.split(DELIMITER)).collect(Collectors.toList());
    }
}
