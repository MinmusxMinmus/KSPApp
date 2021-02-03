package vessels;

import kerbals.Kerbal;
import missions.Mission;
import other.KSPObject;
import other.interfaces.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.CelestialBody;
import other.util.Field;
import other.util.Location;

import java.util.*;
import java.util.stream.Collectors;

// when the vessel crashes, mark all crew as KIA. Pass parameter "Survivor" as a list of kerbals, in case some of them managed to survive.

public class Vessel extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 10; // ALWAYS ACCOUNT FOR DESCRIPTION (IN THIS CASE, FOR TYPE AND ITERATION AS WELL)
    public static final String DELIMITER = ":VI:";

    // Persistent fields
    private final long id;
    private String concept;
    private final int iteration;
    private Location location;
    private final Set<String> crew;
    private final Set<Long> vessels;
    // stranded
    private boolean crashed;
    private String crashDetails;
    private String missionName; // replace with missions

    // Dynamic fields
    private Mission missionObj; // replace with missionObjs
    private final Set<Kerbal> crewObjs = new HashSet<>();
    private final Set<Vessel> vesselObjs = new HashSet<>();
    private Concept conceptObj;

    // Constructors
    /** Defines a new instance of the vessel concept, at the rough location specified.
     * @param id Vessel identifier
     * @param concept Vessel design
     * @param location Location of the craft.
     */
    public Vessel(ControllerInterface controller, long id, Concept concept, Location location, Mission mission, Set<Vessel> vessels, Kerbal... crew) {
        this(controller,
                id,
                concept.getName(),
                concept.getIteration(),
                location,
                Arrays.stream(crew).filter(Objects::nonNull).map(Kerbal::getName).collect(Collectors.toSet()),
                vessels.stream().map(Vessel::getId).collect(Collectors.toSet()),
                false,
                null,
                mission.getName());
        this.missionObj = mission;
    }

    /** Defines a new instance of the vessel concept, as a craft launching from one of Kerbin's launch sites.
     * @param concept Vessel design
     * @param id Vessel identifier
     */
    public Vessel(ControllerInterface controller, Concept concept, long id, Mission mission, Set<Vessel> vessels, Kerbal... crew) {
        this(controller,
                id,
                concept,
                new Location(false, CelestialBody.KERBIN),
                mission,
                vessels,
                crew);
    }

    /** Private implementation. Add params later
     */
    private Vessel(ControllerInterface controller, long id, String concept, int iteration, Location location, Set<String> crew, Set<Long> vessels, boolean crashed, String crashDetails, String missionName) {
        super(controller);
        this.id = id;
        this.concept = concept;
        this.iteration = iteration;
        this.location = location;
        this.crew = crew;
        this.crashed = crashed;
        this.crashDetails = crashDetails;
        this.missionName = missionName;
        this.vessels = vessels;
    }

    /** Generates a new vessel instance from a list of fields stored in persistence.
     * @param fields List of fields
     */
    public Vessel(ControllerInterface controller, List<String> fields) {
        this(controller,
                Long.parseLong(fields.get(1)),
                fields.get(2),
                Integer.parseInt(fields.get(3)),
                Location.fromString(fields.get(4)),
                crewMembersFromString(fields.get(5)),
                vesselsFromString(fields.get(6)),
                Boolean.parseBoolean(fields.get(7)),
                fields.get(8).equals("(none)") ? null : fields.get(8),
                fields.get(9)
        );
        setDescription(fields.get(0));
    }

    private static Set<Long> vesselsFromString(String s) {
        return s.equals("(none)") ? new HashSet<>() : Arrays.stream(s.split(DELIMITER)).map(Long::parseLong).collect(Collectors.toSet());
    }

    private static Set<String> crewMembersFromString(String s) {
        return s.equals("(none)") ? new HashSet<>(): Arrays.stream(s.split(DELIMITER)).collect(Collectors.toSet());
    }


    // Logic methods
    /** Executed whenever a vessel is recovered. This method assumes that the vessel is already in Kerbin surface, and
     * recovers all kerbals inside it.
     */
    public void recover() {
        // Mission end
        this.missionObj = null;
        if (!location.landedAt(CelestialBody.KERBIN)) System.err.println("WARNING: Recovering vessel while not in Kerbin surface! Vessel: " + getName() + ", location " + location.toString());

        // Crew recovery
        for (Kerbal k : crewObjs) {
            k.recover();
            crew.remove(k.getName());
        }

        // Recovery double check
        if (!crewObjs.isEmpty()) System.err.println("WARNING: Vessel was unable to recover all members! Remaining members: " + crewObjs.size());
        if (!crew.isEmpty()) System.err.println("WARNING: Vessel was unable to recover all member names! Remaining member names: " + crew.size());

        // Recover the current vessel
        getController().vesselRecovered(this);
    }

    /** Indicates that the vessel has been destroyed, either by crash or by Tracking Station. Kills all crew inside it.
     * Since only {@link Mission} can call it, it guarantees that the vessel has a mission.
     * @param details Destruction details
     */
    public void crash(String details) {
        // Crew deaths
        for (Kerbal k : crewObjs) {
            float expGained = k.getExpGainedFromCurrentMission();
            k.KIA(k.getMission(), location, expGained, "Died on " + k.getMission().getName() + ", during destruction of " + getName());
        }
        crewObjs.clear();

        // Vessel destruction
        crashed = true;
        crashDetails = details;

        // Last mission
        if (missionObj != null) missionName = missionObj.getName();

        getController().vesselCrashed(this);
    } // Kerbal... victims, Vessel... survivors

    // updateLocation()

    // completeMission()

    // addMission()

    // removeMission()

    public void addVessel(Vessel v) {
        vesselObjs.add(v);
        vessels.add(v.id);
    }

    public void removeVessel(Vessel v) {
        vesselObjs.remove(v);
        vessels.remove(v.id);
    }

    // addCrew()

    // removeCrew()

    // Getter/Setter methods
    public String getName() {
        return conceptObj.getName();
    }

    public VesselType getType() {
        return conceptObj.getType();
    }

    public int getIteration() {
        return conceptObj.getIteration();
    }

    public long getId() {
        return id;
    }

    public String getConcept() {
        return concept;
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<String> getCrew() {
        return new HashSet<>(crew);
    }

    public Set<Long> getVessels() {
        return new HashSet<>(vessels);
    }

    // Overrides
    @Override
    public void ready() {
        // Get vessel mission
        missionObj = getController().getMission(missionName);
        if (missionObj != null) missionObj.addEventListener(this);

        // Get crew
        for (String s : crew) {
            Kerbal k = getController().getKerbal(s);
            if (k != null) {
                k.addEventListener(this);
                crewObjs.add(k);
            }
        }

        // Get concept
        conceptObj = getController().getConcept(concept);
        if (conceptObj != null) conceptObj.addEventListener(this);

        // Get connected vessels
        for (long l : vessels) {
            Vessel v = getController().getInstance(l);
            if (v != null) vesselObjs.add(v);
        }
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(Long.toString(id));
        ret.add(concept);
        ret.add(Integer.toString(iteration));
        ret.add(Location.toString(location));

        StringJoiner joiner = new StringJoiner(DELIMITER);
        crew.forEach(joiner::add);
        if (joiner.toString().equals("")) joiner.add("(none)");
        ret.add(joiner.toString());

        StringJoiner joiner2 = new StringJoiner(DELIMITER);
        vessels.forEach(l -> joiner.add(Long.toString(l)));
        if (joiner.toString().equals("")) joiner.add("(none)");
        ret.add(joiner2.toString());
        ret.add(Boolean.toString(crashed));
        ret.add(crashDetails == null ? "(none)" : crashDetails);
        ret.add(missionName);

        return ret;
    }

    @Override
    public String getTextRepresentation() {
        return concept + " Mk" + getIteration() + ": " + location.toString();
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", concept));
        fields.add(new Field("Iteration", "Mk" + getIteration()));
        fields.add(new Field("ID", Long.toString(id)));
        fields.add(new Field("Concept", concept));
        for (Vessel v : vesselObjs) fields.add(new Field("Connected vessel", v.getName()));
        if (crashed) {
            fields.add(new Field("Last mission", missionName == null ? "None" : missionName));
            fields.add(new Field("Crash location", location.toString()));
            fields.add(new Field("Crash details", crashDetails));
        } else {
            fields.add(new Field("Mission", missionObj == null ? "None" : missionObj.getName()));
            fields.add(new Field("Type", getType().toString()));
            fields.add(new Field("Location", location.toString()));
        }
        for (String s : crew) fields.add(new Field("Crew member", s + " Kerman" + (crashed ? " (KIA)" : "")));


        return fields;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Crew member deleted
        if (event.getSource() instanceof Kerbal k) {
            crewObjs.remove(k);
            crew.remove(k.getName());
        }

        // Concept deleted
        if (event.getSource() instanceof Concept) {
            concept = "[REDACTED]";
            conceptObj = null;
        }

        // Mission deleted
        if (event.getSource() instanceof Mission m) {
            missionObj = null;
            missionName = "[REDACTED]";
        }
    }
}
