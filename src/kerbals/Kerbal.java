package kerbals;

import controller.ControllerInterface;
import missions.Mission;
import other.KSPObject;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.KSPDate;
import other.util.Location;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Kerbals represent the living organisms that decided to pay a visit to the rest of the Kerbol system and its celestial
 * bodies.
 * <p>
 *     Kerbals keep a record of their hiring/rescue date, as well as their origin and a series of lists containing the
 *     kerbal's {@link Condecoration} collection, a list of missions ({@link Mission}) the kerbal is part of, and a list
 *     of previous missions ({@link FlightLog}). Finally, a KIA marker indicates whether the kerbal passed away.
 * </p>
 */
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
     * Indicates whether the kerbal is dead or not.
     */
    private boolean KIA;
    /**
     * The kerbal's current {@link Location}.
     */
    private Location location;
    /**
     * The kerbal's flight log. Contains information regarding all of the past missions.
     */
    private final List<FlightLog> log;
    /**
     * Indicates the mission the kerbal is currently on
     */
    private Set<String> missions;
    /**
     * Honorable mentions that speak to the kerbal's achievements.
     */
    private final List<Condecoration> condecorations;

    // Dynamic fields
    private Mission originObj;
    private Set<Mission> missionObjs;

    // Constructors
    /**
     * Generates a new kerbal.
     * @param name The kerbal's name.
     * @param isMale The kerbal's gender.
     * @param job The kerbal's job.
     * @param hiringDate The date the kerbal was hired / rescued.
     */
    public Kerbal(ControllerInterface controller, String name, boolean isMale, boolean badass, Job job, String origin, KSPDate hiringDate, Location location) {
        super(controller);
        this.name = name;
        this.male = isMale;
        this.badass = badass;
        this.job = job;
        this.origin = origin;
        this.hiringDate = hiringDate;
        this.KIA = false;
        this.location = location;

        this.log = new LinkedList<>();
        this.missions = new HashSet<>();
        this.condecorations = new LinkedList<>();
    }

    /**
     * Generates a kerbal from a list of fields stores in persistence.
     * @param fields List of fields
     */
    public Kerbal(ControllerInterface controller, List<String> fields) {
        super(controller);
        setDescription((fields.get(0)));
        this.name = fields.get(1);
        this.male = Boolean.parseBoolean(fields.get(2));
        this.badass = Boolean.parseBoolean(fields.get(3));
        this.job = Job.fromString(fields.get(4));
        this.origin = fields.get(5);
        this.hiringDate = KSPDate.fromString(controller, fields.get(6));
        this.KIA = Boolean.parseBoolean(fields.get(7));
        this.location = Location.fromString(fields.get(8));
        this.log = fields.get(9).equals("(none)")
                ? new LinkedList<>()
                : Arrays.stream(fields.get(9).split(DELIMITER)).map(s -> FlightLog.fromString(controller, s)).collect(Collectors.toList());
        this.missions = fields.get(10).equals("(none)")
                ? null
                : new HashSet<>(Arrays.asList(fields.get(10).split(DELIMITER)));
        this.condecorations = fields.get(11).equals("(none)")
                ? new LinkedList<>()
                : Arrays.stream(fields.get(11).split(DELIMITER)).map(s -> Condecoration.fromString(controller, s)).collect(Collectors.toList());
    }

    // Logic methods
    /**
     * Executed whenever a kerbal embarks on a new mission
     * @param m Current mission
     */
    public void missionStart(Mission m) {
        missions.add(m.getName());
        missionObjs.add(m);
        m.addEventListener(this);
    }

    // missionEnd()

    /**
     * Executed whenever the kerbal unfortunately goes KIA.
     */
    public void KIA() {
        // Set KIA
        KIA = true;

        // Final log entries
        for (Mission m : missionObjs) {
            FlightLog fl = new FlightLog(getController(), m.getName());
            fl.setDescription("Went KIA during the development of " + m.getName() + ", while " + location.toString());
            log.add(fl);
        }
        missionObjs.clear();
        missions.clear();
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

    public boolean isKIA() {
        return KIA;
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    public List<FlightLog> getLog() {
        return Collections.unmodifiableList(log);
    }

    public List<Condecoration> getCondecorations() {
        return condecorations;
    }

    // Overrides
    @Override
    public void ready() {
        // Set origin
        originObj = getController().getMission(origin);
        if (originObj != null) originObj.addEventListener(this);

        // Set current missions
        missionObjs = new HashSet<>();
        for (String name : missions) {
            Mission m = getController().getMission(name);
            if (m != null) {
                m.addEventListener(this);
                missionObjs.add(m);
            } else if (!name.equals("[REDACTED]")) System.err.println("WARNING: Mission not found! Kerbal: " + name + ", mission name: " + name);
        }
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = super.toStorableCollection();

        StringJoiner logJoiner = new StringJoiner(DELIMITER);
        for (FlightLog log1 : log) logJoiner.add(FlightLog.toString(log1));
        if (logJoiner.toString().equals("")) logJoiner.add("(none)");

        StringJoiner missionJoiner = new StringJoiner(DELIMITER);
        for (String name : missions) missionJoiner.add(name);
        if (missionJoiner.toString().equals("")) missionJoiner.add("(none)");

        StringJoiner condecorationJoiner = new StringJoiner(DELIMITER);
        for (Condecoration c : condecorations) condecorationJoiner.add(Condecoration.toString(c));
        if (condecorationJoiner.toString().equals("")) condecorationJoiner.add("(none)");

        ret.add(name);
        ret.add(Boolean.toString(male));
        ret.add(Boolean.toString(badass));
        ret.add(job.toString());
        ret.add(origin);
        ret.add(hiringDate.toStorableString());
        ret.add(Boolean.toString(KIA));
        ret.add(Location.toString(location));

        ret.add(logJoiner.toString());
        ret.add(missionJoiner.toString());
        ret.add(condecorationJoiner.toString());

        return ret;
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", name + " Kerman" + (KIA ? " (KIA)" : "")));
        fields.add(new Field("Job", job.toString()));
        fields.add(new Field("Gender", male ? "Male" : "Female"));
        fields.add(new Field("Badass?", badass ? "Yes" : "No"));
        fields.add(new Field("Recruitment", origin));
        fields.add(new Field("Recruitment date", hiringDate.toString(true, true)));
        fields.add(new Field("Location", location.toString()));
        for (Mission m : missionObjs) fields.add(new Field("Deployed in:", m.getName()));
        for (FlightLog l : log) fields.add(new Field("Participated in:", l.toString()));
        for (Condecoration c : condecorations) fields.add(new Field("Honorable mention", c.toString()));

        return fields;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deleted
        if (event.getSource() instanceof Mission m) {
            // Active mission
            if (missionObjs.contains(m)) {
                missionObjs.remove(m);
                missions.remove(m.getName());
            }
            // Origin mission
            if (m.equals(originObj)) {
                origin = "[REDACTED]";
                originObj = null;
            }
        }
    }

    @Override
    public String toString() {
        return name + " Kerman (" + job.toString() + "): " + location.toString();
    }
}
