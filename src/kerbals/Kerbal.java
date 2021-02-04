package kerbals;

import missions.Mission;
import other.KSPObject;
import controller.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.KSPDate;
import other.util.Location;

import java.util.*;
import java.util.stream.Collectors;

public class Kerbal extends KSPObject implements KSPObjectListener {

    public static final String DELIMITER = ":k:";
    public static final int ENCODE_FIELD_AMOUNT = 12; // ALWAYS ACCOUNT FOR DESCRIPTION

    // Persistent fields
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
     * The mission where the kerbal was rescued in. Alternatively, the means of hiring.
     */
    private String origin;
    /**
     * The date in which the kerbal joined the space center crew.
     */
    private final KSPDate hiringDate;
    /**
     * The kerbal's flight log. Contains information regarding all of the past missions.
     */
    private final List<FlightLog> log;
    /**
     * Indicates the mission the kerbal is currently on
     */
    private String mission; // replace with missions
    /**
     * Honorable mentions that speak to the kerbal's achievements.
     */
    private final List<Condecoration> condecorations;
    /**
     * The kerbal's total experience
     */
    private float experience; // replace with milestones
    private boolean KIA = false;
    // location

    // Dynamic fields
    private Mission originObj;
    private Mission missionObj; // replace with missionObjs

    // Constructors
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
        this.mission = null;
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

        List<FlightLog> result;
        String field = fields.get(7);
        if (field.equals("(none)")) result = new LinkedList<>();
        else result = Arrays.stream(field.split(DELIMITER)).map(s -> FlightLog.fromString(controller, s)).collect(Collectors.toList());
        this.log = result;
        this.mission = fields.get(8).equals("(none)") ? null :fields.get(8);

        List<Condecoration> result1;
        String fields1 = fields.get(9);
        if (fields1.equals("(none)")) result1 = new LinkedList<>();
        else
            result1 = Arrays.stream(fields1.split(DELIMITER)).map(s -> Condecoration.fromString(controller, s)).collect(Collectors.toList());
        this.condecorations = result1;
        this.experience = Float.parseFloat(fields.get(10));
        this.KIA = Boolean.parseBoolean(fields.get(11));
        setDescription((fields.get(0)));
    }

    // Logic methods
    /** Executed whenever a kerbal embarks on a new mission
     * @param m Current mission
     */
    public void missionStart(Mission m) {
        mission = m.getName();
        missionObj = m;
        m.addEventListener(this);
    } // edit for multiple missions

    // missionEnd()

    /** Executed whenever a kerbal is succesfully recovered.
     */
    public void recover() {
        // Add new entry to flight log, make it a listener, set new experience
        FlightLog fl = new FlightLog(getController(), mission, missionObj.getExperienceGained(this));
        fl.setDescription("Succesfully recovered from " + mission);
        log.add(fl);
        missionObj.addEventListener(fl);
        experience += missionObj.getExperienceGained(this);

        mission = null;
        // No longer interested in this mission
        missionObj.removeEventListener(this);
        missionObj = null;
    } // edit (no mission finish, update milestones)

    /** Executed whenever the kerbal unfortunately goes KIA.
     * @param mission The mission that caused their death
     * @param location Location where the kerbal went KIA
     * @param details Additional KIA details
     */
    public void KIA(Mission mission, Location location, float expGained, String details) {
        // Set KIA
        KIA = true;

        // Final log entry
        FlightLog fl = new FlightLog(getController(), this.mission == null ? "(none)" : this.mission, missionObj == null ? 0.0f : missionObj.getExperienceGained(this));
        fl.setDescription("KIA " + location.toString().substring(0, 1).toLowerCase(Locale.ROOT) + location.toString().substring(1) + " (+" + expGained + "):\n" + details);
        log.add(fl);

        missionObj = null;
    }

    // switchVessel(old, new)

    // leaveVessel()

    // updateLocation()

    // addCondecoration()

    // Getter/Setter methods
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isMale() {
        return male;
    }
    public void setMale(boolean male) {
        this.male = male;
    }

    public boolean isBadass() {
        return badass;
    }
    public void setBadass(boolean badass) {
        this.badass = badass;
    }

    public Job getJob() {
        return job;
    }
    public void setJob(Job job) {
        this.job = job;
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
        return mission == null;
    }

    public Mission getMission() {
        return missionObj;
    }

    public float getExpGainedFromCurrentMission() {
        return missionObj.getExperienceGained(this);
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

    public boolean isKIA() {
        return KIA;
    }

    // Overrides
    @Override
    public void ready() {
        // Set origin
        originObj = getController().getMission(origin);
        if (originObj != null) originObj.addEventListener(this);

        // Set current mission
        missionObj = mission == null ? null : getController().getMission(mission);
        if (missionObj != null) missionObj.addEventListener(this);
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

        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (FlightLog log1 : log) joiner.add(FlightLog.toString(log1));
        ret.add(joiner.toString().equals("") ? "(none)" : joiner.toString());
        ret.add(mission == null ? "(none)" : mission);

        StringJoiner joiner1 = new StringJoiner(DELIMITER);
        for (Condecoration c : condecorations) joiner1.add(Condecoration.toString(c));
        ret.add(joiner1.toString().equals("") ? "(none)" : joiner1.toString());
        ret.add(Float.toString(experience));
        ret.add(Boolean.toString(KIA));

        return ret;
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", name + " Kerman" + (KIA ? " (KIA)" : "")));
        if (KIA) fields.add(new Field("Last mission", log.get(log.size() - 1).getMissionName()));
        fields.add(new Field("Job", job.toString()));
        fields.add(new Field("Gender", male ? "Male" : "Female"));
        fields.add(new Field("Badass?", badass ? "Yes" : "No"));
        fields.add(new Field("Level", Integer.toString(getLevel())));
        fields.add(new Field("Recruitment", origin));
        fields.add(new Field("Recruitment date", hiringDate.getTextRepresentation(false)));
        fields.add(new Field("Deployed?", mission == null ? "No" : "Yes"));
        if (mission != null) fields.add(new Field("Current mission", mission));
        for (FlightLog l : log) fields.add(new Field("Mission", l.getTextRepresentation()));
        for (Condecoration c : condecorations) fields.add(new Field("Honorable mention", c.getTextRepresentation()));

        return fields;
    }

    @Override
    public String getTextRepresentation() {
        return name + " Kerman (" + job.toString() + (mission == null ? ", available)" : ", deployed on " + mission + ")");
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deleted
        if (event.getSource() instanceof Mission m) {
            // Active mission
            if (m.equals(missionObj)) {
                mission = "[REDACTED]";
                missionObj = null;
                System.err.println("WARNING: Kerbal " + name + " got his active mission \"" + m.getName() + "\" removed!");
            }
            // Origin mission
            if (m.equals(originObj)) {
                origin = "[CLASSIFIED]";
                originObj = null;
            }
        }
    }
}
