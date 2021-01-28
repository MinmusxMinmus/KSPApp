package kerbals;

import missions.Mission;
import other.*;

import java.util.*;
import java.util.stream.Collectors;

public class Kerbal extends KSPObject implements Editable, KSPObjectListener {

    public static final String DELIMITER = ":k:";
    public static final int ENCODE_FIELD_AMOUNT = 12; // ALWAYS ACCOUNT FOR DESCRIPTION

    // Persistent information
    /**
     * The kerbal's name. The surname is not included.
     */
    private String name;
    /**
     * True if male, false if female
     */
    private boolean male;
    /**
     * Badass flag
     */
    private boolean badass;
    /**
     * The kerbal's job position.
     */
    private Job job;
    /**
     * The mission where the kerbal was rescued in.
     */
    private String origin;
    /**
     * The date in which the kerbal joined the space center crew.
     */
    private KSPDate hiringDate;
    /**
     * The kerbal's flight log. Contains information regarding all of the past missions.
     */
    private List<FlightLog> log;
    /**
     * Indicates the mission the kerbal is currently on
     */
    private String currentMission;
    /**
     * Honorable mentions that speak to the kerbal's achievements.
     */
    private List<Condecoration> condecorations;
    /**
     * The kerbal's total experience
     */
    private float experience;
    private boolean KIA = false; // TODO add to persistence

    // Dynamic fields
    private Mission originMission;
    private Mission activeMission;


    /** Generates a new kerbal.
     * @param name The kerbal's name.
     * @param isMale The kerbal's gender.
     * @param job The kerbal's job.
     * @param hiringDate The date the kerbal was hired / rescued.
     */
    public Kerbal(ControllerInterface controller, String name, boolean isMale, boolean badass, Job job, String origin, KSPDate hiringDate) {
        super(controller);
        this.name = name;
        this.male = isMale;
        this.badass = badass;
        this.job = job;
        this.origin = origin;
        this.hiringDate = hiringDate;

        this.log = new LinkedList<>();
        this.currentMission = null;
        this.condecorations = new LinkedList<>();
        this.experience = 0;

    }

    /** Generates a kerbal from a list of fields stores in persistence.
     * @param fields List of fields
     */
    public Kerbal(ControllerInterface controller, List<String> fields) {
        super(controller);
        this.name = fields.get(1);
        this.male = Boolean.parseBoolean(fields.get(2));
        this.badass = Boolean.parseBoolean(fields.get(3));
        this.job = Job.fromString(fields.get(4));
        this.origin = fields.get(5);
        this.hiringDate = KSPDate.fromString(controller, fields.get(6));
        this.log = flightLogFromString(controller, fields.get(7));
        this.currentMission = fields.get(8).equals("(none)") ? null :fields.get(8);
        this.condecorations = condecorationsFromString(controller, fields.get(9));
        this.experience = Float.parseFloat(fields.get(10));
        this.KIA = Boolean.parseBoolean(fields.get(11));
        setDescription((fields.get(0)));
    }

    private String flightLogToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (FlightLog log : log) joiner.add(FlightLog.toString(log));
        return joiner.toString().equals("") ? "(none)" : joiner.toString();
    }

    private static List<FlightLog> flightLogFromString(ControllerInterface controller, String field) {
        if (field.equals("(none)")) return new LinkedList<>();
        return Arrays.stream(field.split(DELIMITER)).map(s -> FlightLog.fromString(controller, s)).collect(Collectors.toList());
    }

    private String condecorationsToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (Condecoration c : condecorations) joiner.add(Condecoration.toString(c));
        return joiner.toString().equals("") ? "(none)" : joiner.toString();
    }

    private static List<Condecoration> condecorationsFromString(ControllerInterface c, String fields) {
        if (fields.equals("(none)")) return new LinkedList<>();
        return Arrays.stream(fields.split(DELIMITER)).map(s -> Condecoration.fromString(c, s)).collect(Collectors.toList());
    }


    /** Executed whenever a kerbal embarks on a new mission
     * @param m Current mission
     */
    public void missionStart(Mission m) {
        currentMission = m.getName();
        activeMission = m;
    }

    /** Executed whenever a kerbal is succesfully recovered back from a mission.
     */
    public void recover() {
        // Add new entry to flight log, set new experience
        FlightLog fl = new FlightLog(getController(), currentMission, activeMission.getExperienceGained(this));
        fl.setDescription("Succesfully recovered from " + currentMission);
        log.add(fl);
        experience += activeMission.getExperienceGained(this);

        currentMission = null;
        activeMission = null;
    }

    /** Executed whenever the kerbal unfortunately goes KIA.
     * @param mission The mission that caused their death
     * @param inSpace True if the kerbal died in space, false otherwise
     * @param location Celestial body's SoI where the kerbal went KIA
     * @param details Additional KIA details
     */
    public void KIA(Mission mission, boolean inSpace, CelestialBody location, float expGained, String details) {
        // Set KIA
        KIA = true;

        // Final log entry
        FlightLog fl = new FlightLog(getController(), currentMission == null ? "(none)" : currentMission, activeMission == null ? 0.0f : activeMission.getExperienceGained(this));
        fl.setDescription("KIA in " + (inSpace ? "orbit of  " : "") + location.toString() + " (+" + expGained + "):\n" + details);
        log.add(fl);

        activeMission = null;
    }

    public String getName() {
        return name;
    }

    public boolean isMale() {
        return male;
    }

    public boolean isBadass() {
        return badass;
    }

    public void setBadass(boolean badass) {
        this.badass = badass;
    }

    public Job getRole() {
        return job;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public KSPDate getHiringDate() {
        return hiringDate;
    }

    public List<FlightLog> getLog() {
        return Collections.unmodifiableList(log);
    }

    public boolean isAvailable() {
        return currentMission == null;
    }

    public Mission getCurrentMission() {
        return activeMission;
    }

    public float getExpGainedFromCurrentMission() {
        return activeMission.getExperienceGained(this);
    }

    public int getLevel() {
        if (experience > 64.0) return 5;
        if (experience > 32.0) return 4;
        if (experience > 16.0) return 3;
        if (experience > 8.0) return 2;
        if (experience > 2.0) return 1;
        return 0;
    }

    public List<Condecoration> getCondecorations() {
        return condecorations;
    }

    public float getExperience() {
        return experience;
    }

    @Override
    public void ready() {
        // Set origin
        originMission = getController().getMission(origin);

        // Set current mission
        activeMission = currentMission == null ? null : getController().getMission(currentMission);
    }

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
        ret.add(currentMission == null ? "(none)" : currentMission);
        ret.add(condecorationsToString());
        ret.add(Float.toString(experience));
        ret.add(Boolean.toString(KIA));

        return ret;
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        // TODO update with KIA information

        fields.add(new Field("Name", name + " Kerman"));
        fields.add(new Field("Job", job.toString()));
        fields.add(new Field("Gender", male ? "Male" : "Female"));
        fields.add(new Field("Badass?", badass ? "Yes" : "No"));
        fields.add(new Field("Level", Integer.toString(getLevel())));
        fields.add(new Field("Recruitment", origin));
        fields.add(new Field("Recruitment date", hiringDate.getTextRepresentation(false)));
        fields.add(new Field("Deployed?", currentMission == null ? "No" : "Yes"));
        if (currentMission != null) fields.add(new Field("Current mission", currentMission));
        for (FlightLog l : log) fields.add(new Field("Mission", l.getTextRepresentation()));
        for (Condecoration c : condecorations) fields.add(new Field("Honorable mention", c.getTextRepresentation()));

        return fields;
    }

    @Override
    public String getTextRepresentation() {
        return name + " Kerman (" + job.toString() + (currentMission == null ? ", available)" : ", deployed on " + currentMission + ")");
    }

    @Override
    public boolean isEditable(int rowIndex) {
        return false;
    }

    @Override
    public boolean setField(int index, Object object) {
        return false;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deleted: origin classified
        if (event.getSource() instanceof Mission) origin = "[CLASSIFIED]";
    }

    public boolean isKIA() {
        return KIA;
    }
}
