package kerbals;

import controller.ControllerInterface;
import missions.Mission;
import other.KSPObject;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.KSPDate;
import other.util.Location;
import vessels.Vessel;

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
    public static final int ENCODE_FIELD_AMOUNT = 13; // ALWAYS ACCOUNT FOR DESCRIPTION

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
     * The identifier of the {@link Vessel} the kerbal is currently in. A value of 0 indicates no vessel.
     */
    private long vessel;
    /**
     * The kerbal's flight log. Contains information regarding all of the past missions.
     */
    private final List<FlightLog> log;
    /**
     * Indicates the mission the kerbal is currently on
     */
    private final List<String> missions;
    /**
     * Honorable mentions that speak to the kerbal's achievements.
     */
    private final List<Condecoration> condecorations;

    // Dynamic fields
    private Mission originObj;
    private Vessel vesselObj;
    private List<Mission> missionObjs;

    // Constructors
    /**
     * Generates a new kerbal.
     * @param name The kerbal's name
     * @param isMale Whether the kerbal is male or female
     * @param badass Whether the kerbal is badass or not
     * @param job The kerbal's {@link Job}
     * @param origin The mission the kerbal originates from. Alternatively, the reason behind the hiring.
     * @param hiringDate The {@link KSPDate} at which the kerbal was hired.
     * @param location The {@link Location} of the kerbal.
     * @param vessel The {@link Vessel} the kerbal is currently on.
     */
    public Kerbal(ControllerInterface controller, String name, boolean isMale, boolean badass, Job job, String origin, KSPDate hiringDate, Location location, Vessel vessel) {
        super(controller);
        this.name = name;
        this.male = isMale;
        this.badass = badass;
        this.job = job;
        this.origin = origin;
        this.hiringDate = hiringDate;
        this.KIA = false;
        this.location = location;
        this.vessel = vessel == null ? 0 : vessel.getId();

        this.log = new LinkedList<>();
        this.missions = new LinkedList<>();
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
        this.vessel = Long.parseLong(fields.get(9));
        this.log = fields.get(10).equals("(none)")
                ? new LinkedList<>()
                : Arrays.stream(fields.get(10).split(DELIMITER)).map(s -> FlightLog.fromString(controller, s)).collect(Collectors.toList());
        this.missions = fields.get(11).equals("(none)")
                ? new LinkedList<>()
                : new LinkedList<>(Arrays.asList(fields.get(11).split(DELIMITER)));
        this.condecorations = fields.get(12).equals("(none)")
                ? new LinkedList<>()
                : Arrays.stream(fields.get(12).split(DELIMITER)).map(s -> Condecoration.fromString(controller, s)).collect(Collectors.toList());
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

    /**
     * Executed whenever a kerbal succesfully ends a mission.
     * @param m Mission in question.
     */
    public void missionEnd(Mission m, String comment) {
        missionObjs.remove(m);
        missions.remove(m.getName());
        FlightLog flightLog = new FlightLog(getController(), m.getName());
        flightLog.setDescription("Mission end: " + comment);
        log.add(flightLog);
    }

    /**
     * Executed whenever the kerbal unfortunately goes KIA.
     */
    public void KIA() {
        // Set KIA
        KIA = true;

        // If the kerbal died while inside of a vessel, remain inside of it.

        // Final log entries
        for (Mission m : missionObjs) {
            FlightLog fl = new FlightLog(getController(), m.getName());
            fl.setDescription("Went KIA during the development of " + m.getName() + ", while " + location.toString());
            log.add(fl);
        }
        missionObjs.clear();
        missions.clear();
    }

    public void enterVessel(Vessel v) {
        v.addCrew(this);
        vessel = v.getId();
        vesselObj = v;
        v.addEventListener(this);
    }

    public void leaveVessel(Vessel v) {
        if (vessel != v.getId()) System.err.println("WARNING: Kerbal leaving a vessel he's not in! Kerbal: " + name + ", vessel ID: " + v.getId());
        v.removeCrew(this);
        vessel = 0;
        vesselObj = null;
        v.removeEventListener(this);
    }


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

    public long getVessel() {
        return vessel;
    }

    public List<FlightLog> getLog() {
        return Collections.unmodifiableList(log);
    }
    public boolean addLog(FlightLog log) {
        this.log.add(log);
        return true;
    }
    public boolean removeLog(FlightLog log) {
        return this.log.remove(log);
    }

    public Set<Mission> getMissions() {
        return new HashSet<>(missionObjs);
    }

    public List<Condecoration> getCondecorations() {
        return condecorations;
    }
    public boolean addCondecoration(Condecoration condecoration) {
        condecorations.add(condecoration);
        return true;
    }
    public boolean removeCondecoration(Condecoration condecoration) {
        return condecorations.remove(condecoration);
    }

    // Overrides
    @Override
    public void ready() {
        // Set origin
        originObj = getController().getMission(origin);
        if (originObj != null) {
            origin = "[REDACTED]";
            originObj.addEventListener(this);
        }

        // Set current missions
        missionObjs = new LinkedList<>();
        for (String name : missions) {
            Mission m = getController().getMission(name);
            if (m != null) {
                m.addEventListener(this);
                missionObjs.add(m);
            } else if (!name.equals("[REDACTED]")) System.err.println("WARNING: Mission not found! Kerbal: " + name + ", mission name: " + name);
        }

        // Set current vessel
        vesselObj = getController().getInstance(vessel);
        if (vesselObj == null) vesselObj = getController().getCrashedInstance(vessel);
        if (vesselObj != null) {
            vesselObj.addEventListener(this);
        } else if (vessel != 0) System.err.println("WARNING: Kerbal aboard an unknown vessel! Kerbal: " + name + ", vessel ID = " + vessel);

        // Reaady flight logs
        log.forEach(FlightLog::ready);
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
        ret.add(Long.toString(vessel));

        ret.add(logJoiner.toString());
        ret.add(missionJoiner.toString());
        ret.add(condecorationJoiner.toString());

        return ret;
    }

    @Override
    public boolean isComplexField(int index) {
        if (index < 4) return false;
        if (index == 4) return true;
        return index >= 6;
    }

    @Override
    public KSPObject getComplexField(int index) {
        int missioni = missionObjs.size();
        int logi = log.size();
        int condi = condecorations.size();

        if (index == 4) return originObj; // Origin
        if (index <= 6) return null; // Any other lower field
        if (index <= 6 + missioni) return missionObjs.get(index - 7); // Current mission
        if (index <= 6 + missioni + logi) return log.get(index - 7 -missioni); // Flight log
        if (index <= 6 + missioni + logi + condi) return condecorations.get(index - 7 - missioni - logi); // Condecorations
        if (index == 6 + missioni + logi + condi + 1) return vesselObj; // Current vessel
        return null;
    }

    @Override
    public boolean isTextField(int index) {
        return false;
    }

    @Override
    public String getText(int index) {
        return null;
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
        for (Mission m : missionObjs) fields.add(new Field("Deployed in", m.getName() + " (" + m.getCrewDetails(this).getPosition() + ")"));
        for (FlightLog l : log) fields.add(new Field("Participated in", l.getMissionName()));
        for (Condecoration c : condecorations) fields.add(new Field("Honorable mention", c.toString()));
        if (vessel != 0) fields.add(new Field("Current vessel", vesselObj.getName()));

        return fields;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deleted: either an active mission, or the origin one
        if (event.getSource() instanceof Mission m) {
            // Active missions
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

        // Vessel deleted: leave vessel
        if (event.getSource() instanceof Vessel v) {
            if (vessel != v.getId()) System.err.println("WARNING: Kerbal leaving a vessel he's not in! Kerbal: " + name + ", vessel ID: " + v.getId());
            v.removeCrew(this);
            vessel = 0;
            vesselObj = null;
        }
    }

    @Override
    public String toString() {
        return (KIA ? "(KIA) " : "") + name + " Kerman (" + job.toString() + "): " + location.toString();
    }
}
