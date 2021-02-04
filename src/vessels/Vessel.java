package vessels;

import kerbals.Kerbal;
import missions.Mission;
import other.KSPObject;
import controller.ControllerInterface;
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
    private final Set<String> missions; // replace with missions

    // Dynamic fields
    private Set<Mission> missionObjs; // replace with missionObjs
    private Set<Kerbal> crewObjs = new HashSet<>();
    private Set<Vessel> vesselObjs = new HashSet<>();
    private Concept conceptObj;

    // Constructors
    /** Creates a new instance of the vessel concept, at the rough location specified.
     * @param concept Vessel design
     * @param location Location of the craft.
     */
    public Vessel(ControllerInterface controller, Concept concept, Location location, Set<Vessel> vessels, Kerbal... crew) {
        super(controller);
        this.id = controller.rng();
        this.concept = concept.getName();
        this.iteration = concept.getIteration();
        this.location = location;
        this.crew = Arrays.stream(crew).map(Kerbal::getName).collect(Collectors.toSet());
        this.vessels = vessels.stream().map(Vessel::getId).collect(Collectors.toSet());
        this.crashed = false;
        this.crashDetails = null;
        this.missions = new HashSet<>();
        this.missionObjs = null;
    }

    /** Generates a new vessel instance from a list of fields stored in persistence.
     * @param fields List of fields
     */
    public Vessel(ControllerInterface controller, List<String> fields) {
        super(controller);
        setDescription(fields.get(0));
        this.id = Long.parseLong(fields.get(1));
        this.concept = fields.get(2);
        this.iteration = Integer.parseInt(fields.get(3));
        this.location = Location.fromString(fields.get(4));
        this.crew = fields.get(5).equals("(none)") ? new HashSet<>() : Arrays.stream(fields.get(5).split(DELIMITER)).collect(Collectors.toSet());
        this.vessels = fields.get(6).equals("(none)") ? new HashSet<>() : Arrays.stream(fields.get(6).split(DELIMITER)).map(Long::parseLong).collect(Collectors.toSet());
        this.crashed = Boolean.parseBoolean(fields.get(7));
        this.crashDetails = fields.get(8).equals("(none)") ? null : fields.get(8);
        this.missions = fields.get(9).equals("(none)") ? new HashSet<>() : new HashSet<>(Arrays.asList(fields.get(9).split(DELIMITER)));
    }


    // Logic methods

    // recover()

    // crash(Kerbal... victims, Vessel... survivors)

    // updateLocation()

    // completeMission()

    public void addMission(Mission m) {
        missions.add(m.getName());
        missionObjs.add(m);
    }

    public void removeMission(Mission m) {
        missionObjs.remove(m);
        missions.remove(m.getName());
    }

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
        // Get vessel missions
        missionObjs = new HashSet<>();
        for (Mission m : getController().getMissions()) if (missions.contains(m.getName())) {
            missionObjs.add(m);
            m.addEventListener(this);
        }

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
        vessels.forEach(l -> {
            joiner2.add(Long.toString(l));
            System.out.println("Long: " + l);
        });
        if (joiner2.toString().equals("")) joiner2.add("(none)");
        ret.add(joiner2.toString());

        ret.add(Boolean.toString(crashed));
        ret.add(crashDetails == null ? "(none)" : crashDetails);

        StringJoiner joiner3 = new StringJoiner(DELIMITER);
        for (String name : missions) joiner3.add(name);
        if (joiner3.toString().equals("")) joiner3.add("(none)");
        ret.add(joiner3.toString());

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
        for (Mission m : missionObjs) fields.add(new Field("Mission", m.getName()));
        if (crashed) {
            fields.add(new Field("Crash location", location.toString()));
            fields.add(new Field("Crash details", crashDetails));
        } else {
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
            removeMission(m);
        }
    }
}
