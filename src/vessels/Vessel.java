package vessels;

import kerbals.Kerbal;
import missions.Mission;
import other.KSPObject;
import other.interfaces.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.CelestialBody;
import other.util.Field;

import java.util.*;
import java.util.stream.Collectors;

// TODO when the vessel crashes, mark all crew as KIA. Pass parameter "Survivor" as a list of kerbals, in case some of them managed to survive.

public class Vessel extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 10; // ALWAYS ACCOUNT FOR DESCRIPTION (IN THIS CASE, FOR TYPE AND ITERATION AS WELL)
    public static final String DELIMITER = ":VI:";

    // Persistent fields
    private final long id;
    private String concept;
    private final int iteration;
    private boolean inSpace;
    private CelestialBody location;
    private final Set<String> crew;
    private boolean crashed;
    private String crashDetails;
    private String missionName;

    // Dynamic fields
    private Mission missionObj;
    private final Set<Kerbal> crewObjs = new HashSet<>();
    private Concept conceptObj;

    // Constructors
    /** Defines a new instance of the vessel concept, at the rough location specified.
     * @param id Vessel identifier
     * @param concept Vessel design
     * @param inSpace True if the craft is in space, false otherwise (surface of a celestial body, atmosphere, etc)
     * @param location Celestial body the craft is located at.
     */
    public Vessel(ControllerInterface controller, long id, Concept concept, boolean inSpace, CelestialBody location, Mission mission, Kerbal... crew) {
        this(controller,
                id,
                concept.getName(),
                concept.getIteration(),
                inSpace,
                location,
                Arrays.stream(crew).filter(Objects::nonNull).map(Kerbal::getName).collect(Collectors.toSet()),
                false,
                null,
                mission.getName());
        this.missionObj = mission;
    }

    /** Defines a new instance of the vessel concept, as a craft launching from one of Kerbin's launch sites.
     * @param concept Vessel design
     * @param id Vessel identifier
     */
    public Vessel(ControllerInterface controller, Concept concept, long id, Mission mission, Kerbal... crew) {
        this(controller,
                id,
                concept,
                false,
                CelestialBody.KERBIN,
                mission,
                crew);
    }

    /** Private implementation. Add params later
     */
    private Vessel(ControllerInterface controller, long id, String concept, int iteration, boolean inSpace, CelestialBody location, Set<String> crew, boolean crashed, String crashDetails, String missionName) {
        super(controller);
        this.id = id;
        this.concept = concept;
        this.iteration = iteration;
        this.inSpace = inSpace;
        this.location = location;
        this.crew = crew;
        this.crashed = crashed;
        this.crashDetails = crashDetails;
        this.missionName = missionName;
    }

    /** Generates a new vessel instance from a list of fields stored in persistence.
     * @param fields List of fields
     */
    public Vessel(ControllerInterface controller, List<String> fields) {
        this(controller,
                Long.parseLong(fields.get(1)),
                fields.get(2),
                Integer.parseInt(fields.get(3)),
                Boolean.parseBoolean(fields.get(4)),
                CelestialBody.valueOf(fields.get(5)),
                crewMembersFromString(fields.get(6)),
                Boolean.parseBoolean(fields.get(7)),
                fields.get(8).equals("(none)") ? null : fields.get(8),
                fields.get(9)
        );
        setDescription(fields.get(0));
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
        if (inSpace && location != CelestialBody.KERBIN) System.err.println("WARNING: Recovering vessel while not in Kerbin surface! Vessel: " + getName() + ", location " + location + ", in space? " + inSpace);

        // Crew recovery
        for (Kerbal k : crewObjs) {
            k.recover();
            crew.remove(k.getName());
        }

        // Recovery double check
        if (!crewObjs.isEmpty()) System.err.println("WARNING: Vessel was unable to recover all members! Remaining members: " + crewObjs.size());
        if (!crew.isEmpty()) System.err.println("WARNING: Vessel was unable to recover all member names! Remaining member names: " + crew.size());

        // Recover the current vessel
        getController().instanceRecovered(this);
    }

    /** Indicates that the vessel has been destroyed, either by crash or by Tracking Station. Kills all crew inside it.
     * Since only {@link Mission} can call it, it guarantees that the vessel has a mission.
     * @param details Destruction details
     */
    public void crash(String details) {
        // Crew deaths
        for (Kerbal k : crewObjs) {
            float expGained = k.getExpGainedFromCurrentMission();
            k.KIA(k.getMission(), inSpace, location, expGained, "Died on " + k.getMission().getName() + ", during destruction of " + getName());
        }
        crewObjs.clear();

        // Vessel destruction
        crashed = true;
        crashDetails = details;

        // Last mission
        if (missionObj != null) missionName = missionObj.getName();

        getController().instanceCrashed(this);
    }

    // TODO updatePosition()

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

    public boolean isInSpace() {
        return inSpace;
    }
    public void setInSpace(boolean inSpace) {
        this.inSpace = inSpace;
    }

    public CelestialBody getLocation() {
        return location;
    }
    public void setLocation(CelestialBody location) {
        this.location = location;
    }

    public Set<String> getCrew() {
        return new HashSet<>(crew);
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
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(Long.toString(id));
        ret.add(concept);
        ret.add(Integer.toString(iteration));
        ret.add(Boolean.toString(inSpace));
        ret.add(location.name());
        StringJoiner joiner = new StringJoiner(DELIMITER);
        crew.forEach(joiner::add);
        if (joiner.toString().equals("")) joiner.add("(none)");
        ret.add(joiner.toString());
        ret.add(Boolean.toString(crashed));
        ret.add(crashDetails == null ? "(none)" : crashDetails);
        ret.add(missionName);

        return ret;
    }

    @Override
    public String getTextRepresentation() {
        return concept + " Mk" + getIteration() + (crashed
                        ? ": Crashed " + (inSpace ? "in orbit of " : "on ") + location.toString()
                        : (inSpace ? ": Orbiting ": ": Landed on ") + location.toString());
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", concept));
        fields.add(new Field("Iteration", "Mk" + getIteration()));
        fields.add(new Field("ID", Long.toString(id)));
        fields.add(new Field("Concept", concept));
        if (crashed) {
            fields.add(new Field("Last mission", missionName == null ? "None" : missionName));
            fields.add(new Field("Crash location", (inSpace ? "Orbit of " : "") + location.toString()));
            fields.add(new Field("Crash details", crashDetails));
        } else {
            fields.add(new Field("Mission", missionObj == null ? "None" : missionObj.getName()));
            fields.add(new Field("Type", getType().toString()));
            fields.add(new Field("Location", inSpace ? "Orbiting " : "Landed on " + location.toString()));
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
