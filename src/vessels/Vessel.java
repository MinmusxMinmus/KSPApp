package vessels;

import controller.ControllerInterface;
import kerbals.Kerbal;
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
 * The Vessel class represents a {@link Concept} instance in the Kerbol system. Vessels are used to keep track of actual
 * crafts flying around in KSP, crashed vessels that no longer exist, etc.
 * <p>
 *     A vessel is defined by a {@code long} identifier. This identifier is decided during vessel creation, and is generated
 *     using the current real life time as the {@link Random} generator object's seed. This should pretty much guarantee a
 *     different identifier for every vessel ever created.
 * </p>
 *
 * <p>
 *     Vessels keep track of their {@link Location}, their {@link Kerbal} crew, the different {@link Mission} instances
 *     they participate in, and a list of vessels they are attached/docked to.
 * </p>
 */
public class Vessel extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 12; // ALWAYS ACCOUNT FOR DESCRIPTION
    public static final String DELIMITER = ":VI:";

    // Persistent fields
    /**
     * The vessel's unique identifier.
     */
    private final long id;
    /**
     * The name of the {@link Concept} this vessel's linked to.
     */
    private String concept;
    /**
     * The vessel's iteration.
     */
    private final int iteration;
    /**
     * The vessel's currentl {@link Location}.
     */
    private Location location;
    /**
     * The vessel's {@link VesselStatus}.
     */
    private VesselStatus status;
    /**
     * Contains information regarding the vessel's status.
     */
    private String statusDetails;
    /**
     * The vessel's creation date.
     */
    private final KSPDate creationDate;
    /**
     * The name of the vessel's last mission, before crashing
     */
    private String lastMission;

    /**
     * Contains all of the vessel's crew members' names.
     */
    private final Set<String> crew;
    /**
     * Contains all of the currently connected/docked vessel to this one, as represented by their identifiers.
     */
    private final Set<Long> vessels;
    /**
     * Contains the names of all missions this vessel is currently participating in.
     */
    private final Set<String> missions;

    // Dynamic fields
    /**
     * Contains the {@link Concept} instance of the {@link Vessel#concept} attribute.
     */
    private Concept conceptObj;
    /**
     * Contains all {@link Kerbal} instances of the {@link Vessel#crew} attribute.
     */
    private Set<Kerbal> crewObjs;
    /**
     * Contains all vessel instances of the {@link Vessel#vessels} attribute.
     */
    private Set<Vessel> vesselObjs;
    /**
     * Contains all {@link Mission} instances of the {@link Vessel#missions} attribute.
     */
    private Set<Mission> missionObjs;

    // Constructors
    /**
     * Creates a new instance of the vessel concept, at the location specified.
     * <br>
     *     <p>A vessel can be created anywhere, at any time. The most common use case is when launching a new vessel, but
     *     it is also possible to construct a brand new vessel in space using EVA Construction Mode. Spawning in a
     *     vessel is also an uncommon (but doable) case.</p>
     * @param controller App controller, used to dynamically link objects
     * @param concept Vessel design
     * @param location Location of the craft
     * @param vessels List of connected vessels
     * @param crew The vessel's crew members
     */
    public Vessel(ControllerInterface controller, Concept concept, KSPDate creationDate, Location location, Set<Vessel> vessels, Kerbal... crew) {
        super(controller);
        this.id = controller.rng();
        this.concept = concept.getName();
        this.iteration = concept.getIteration();
        this.location = location;
        this.status = VesselStatus.NOMINAL;
        this.statusDetails = null;
        this.creationDate = creationDate;
        this.crew = Arrays.stream(crew).map(Kerbal::getName).collect(Collectors.toSet());
        this.vessels = vessels.stream().map(Vessel::getId).collect(Collectors.toSet());
        this.missions = new HashSet<>();
        this.missionObjs = null;
    }

    /**
     * Generates a new vessel instance from a list of fields stored in persistence.
     * @param controller App controller, used to dynamically link objects
     * @param fields List of fields
     */
    public Vessel(ControllerInterface controller, List<String> fields) {
        super(controller);
        setDescription(fields.get(0));
        this.id = Long.parseLong(fields.get(1));
        this.concept = fields.get(2);
        this.iteration = Integer.parseInt(fields.get(3));
        this.location = Location.fromString(fields.get(4));
        this.status = VesselStatus.valueOf(fields.get(5));
        this.statusDetails = fields.get(6).equals("(none)") ? null : fields.get(6);
        this.creationDate = KSPDate.fromString(controller, fields.get(7));
        this.lastMission = fields.get(8);
        this.crew = fields.get(9).equals("(none)") ? new HashSet<>() : Arrays.stream(fields.get(9).split(DELIMITER)).collect(Collectors.toSet());
        this.vessels = fields.get(10).equals("(none)") ? new HashSet<>() : Arrays.stream(fields.get(10).split(DELIMITER)).map(Long::parseLong).collect(Collectors.toSet());
        this.missions = fields.get(11).equals("(none)") ? new HashSet<>() : new HashSet<>(Arrays.asList(fields.get(11).split(DELIMITER)));
    }


    // Logic methods
    /**
     * Marks the vessel as crashed, kills all the crew inside and destroys all vessels connected to it. This method
     * assumes total destruction, so if any vessels or crew members survive make sure to remove them from the vessel
     * before calling this method
     * @param details Details regarding the crash
     */
    public void setCrashed(KSPDate date, String lastMission, String details) {
        // Set crash status
        setStatus(VesselStatus.CRASHED);
        this.lastMission = lastMission;
        // Kill all crew members
        for (Kerbal k : crewObjs) k.KIA();
        // Destroy all vessels. Makes sure to not be recursive.
        for (Vessel v : vesselObjs) if (v.status != VesselStatus.CRASHED) v.setCrashed(date, lastMission, details);
        // Remove all missions, log the vessel crash of course
        for (Mission m : getMissions()) {
            m.logEvent(location, date, "\"" + getName() + "\" vessel crashed: " + details);
            missionEnd(m);
        }
        // Set details
        setStatusDetails(details);
    }

    /**
     * Marks the vessel as stranded, which marks all connected vessels as such.
     * @param details Details regarding the situation.
     */
    public void setStranded(String details) {
        // Set stranded status
        setStatus(VesselStatus.STRANDED);
        // Marks all vessels as stranded. Makes sure to not be recursive
        for (Vessel v : vesselObjs) if (v.status != VesselStatus.STRANDED) v.setStranded(details);
        setStatusDetails(details);
    }

    public void missionStart(Mission m) {
        missions.add(m.getName());
        missionObjs.add(m);
        m.addEventListener(this);
    }

    public void missionEnd(Mission m) {
        missionObjs.remove(m);
        missions.remove(m.getName());
        m.removeEventListener(this);
    }


    // Getter/Setter methods
    /**
     * Returns the vessel's name, as defined by the {@link Concept} it is linked to. If the concept is missing due to
     * deletion, the name defaults to "[REDACTED]".
     */
    public String getName() {
        if (conceptObj == null) return "[REDACTED]";
        return conceptObj.getName();
    }

    /**
     * Returns the vessel's {@link VesselType}, as defined by the {@link Concept} it is linked to. If the concept is
     * missing due to deletion, the type defaults to {@link VesselType#UNKNOWN}.
     */
    public VesselType getType() {
        if (conceptObj == null) return VesselType.UNKNOWN;
        return conceptObj.getType();
    }

    /**
     * Returns the vessel's iteration. This iteration is independent to the one the vessel's {@link Concept} is
     * currently at. This allows for old versions of the concept to roam around in the Kerbol system due to previous
     * launches.
     */
    public int getIteration() {
        return iteration;
    }

    public long getId() {
        return id;
    }

    /**
     * Returns the {@link Concept} the vessel is linked to. A vessel's concept defines the ship's characteristics, its
     * purpose, and other technical details of the craft.
     */
    public String getConcept() {
        return concept;
    }

    /**
     * Returns the {@link Location} the vessel is currently at.
     */
    public Location getLocation() {
        return location;
    }
    /**
     * Updates the vessel's location, as well as that of the crew and connected vessels.
     * @return {@code true} by default
     */
    public boolean setLocation(Location location) {
        this.location = location;
        // Change crew locations
        for (Kerbal k : crewObjs) k.setLocation(location);
        // Change vessel locations. Make sure to not be recursive!
        for (Vessel v : vesselObjs) if (v.location != location) v.setLocation(location);
        return true;
    }

    public VesselStatus getStatus() {
        return status;
    }
    /**
     * @return {@code true} by default
     */
    public boolean setStatus(VesselStatus status) {
        this.status = status;
        return true;
    }

    public String getStatusDetails() {
        return statusDetails;
    }
    /**
     * @return {@code true} by default
     */
    public boolean setStatusDetails(String details) {
        this.statusDetails = details;
        return true;
    }

    public KSPDate getCreationDate() {
        return creationDate;
    }

    public Set<Kerbal> getCrew() {
        return new HashSet<>(crewObjs);
    }

    /**
     * @return {@code true} by default
     */
    public boolean addCrew(Kerbal k) {
        crewObjs.add(k);
        crew.add(k.getName());
        k.addEventListener(this);
        return true;
    }
    /**
     * @return {@code true} by default
     */
    public boolean removeCrew(Kerbal k) {
        crewObjs.remove(k);
        crew.remove(k.getName());
        k.removeEventListener(this);
        return true;
    }

    public Set<Vessel> getVessels() {
        return new HashSet<>(vesselObjs);
    }
    /**
     * @return {@code true} by default
     */
    public boolean addVessel(Vessel v) {
        vesselObjs.add(v);
        vessels.add(v.id);
        v.addEventListener(this);
        return true;
    }
    /**
     * @return {@code true} by default
     */
    public boolean removeVessel(Vessel v) {
        vesselObjs.remove(v);
        vessels.remove(v.id);
        v.removeEventListener(this);
        return true;
    }

    public Set<Mission> getMissions() {
        return new HashSet<>(missionObjs);
    }

    public boolean inMission(Mission m) {
        return missionObjs.contains(m);
    }

    // Overrides
    @Override
    public void ready() {
        // Get concept
        if (!concept.equals("[REDACTED]")) {
            conceptObj = getController().getConcept(concept);
            if (conceptObj != null) conceptObj.addEventListener(this);
            else System.err.println("WARNING: Vessel concept not found! Vessel: " + id + ", concept name: " + concept);
        }

        // Get missions
        missionObjs = new HashSet<>();
        for (String name : missions) {
            Mission mission = getController().getMission(name);
            if (mission != null) {
                mission.addEventListener(this);
                missionObjs.add(mission);
            } else System.err.println("WARNING: Vessel mission not found! Vessel: " + id + ", mission: " + name);
        }

        // Get crew
        crewObjs = new HashSet<>();
        for (String name : crew) {
            Kerbal kerbal = getController().getKerbal(name);
            if (kerbal != null) {
                kerbal.addEventListener(this);
                crewObjs.add(kerbal);
            } else System.err.println("WARNING: Vessel crew member not found! Vessel: " + id + ", kerbal name: " + name);
        }

        // Get vessels
        vesselObjs = new HashSet<>();
        for (long id : vessels) {
            Vessel vessel = getController().getInstance(id);
            if (vessel != null) {
                vesselObjs.add(vessel);
                vessel.addEventListener(this);
            } else System.err.println("WARNING: Vessel connected vessel not found! Vessel: " + this.id + ", connected vessel ID: " + id);
        }
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        StringJoiner crewJoiner = new StringJoiner(DELIMITER);
        crew.forEach(crewJoiner::add);
        if (crewJoiner.toString().equals("")) crewJoiner.add("(none)");

        StringJoiner vesselJoiner = new StringJoiner(DELIMITER);
        vessels.forEach(id -> vesselJoiner.add(Long.toString(id)));
        if (vesselJoiner.toString().equals("")) vesselJoiner.add("(none)");

        StringJoiner missionJoiner = new StringJoiner(DELIMITER);
        for (String name : missions) missionJoiner.add(name);
        if (missionJoiner.toString().equals("")) missionJoiner.add("(none)");

        ret.add(Long.toString(id));
        ret.add(concept);
        ret.add(Integer.toString(iteration));
        ret.add(Location.toString(location));
        ret.add(status.name());
        ret.add(statusDetails == null ? "(none)" : statusDetails);
        ret.add(creationDate.toStorableString());
        ret.add(lastMission);
        ret.add(crewJoiner.toString());
        ret.add(vesselJoiner.toString());
        ret.add(missionJoiner.toString());

        return ret;
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", concept));
        fields.add(new Field("Iteration", "Mk" + getIteration()));
        fields.add(new Field("Type", getType().toString()));
        fields.add(new Field("ID", Long.toString(id)));
        fields.add(new Field("Concept", concept));
        fields.add(new Field("Status", status.toString()));
        if (status.equals(VesselStatus.CRASHED)) {
            fields.add(new Field("Crash details", statusDetails));
            fields.add(new Field("Last mission", lastMission));
        }
        fields.add(new Field("Location", location.toString()));
        for (Kerbal k : crewObjs) fields.add(new Field("Crew member", k.toString()));
        for (Vessel v : vesselObjs) fields.add(new Field("Connected vessel", v.getName()));
        for (Mission m : missionObjs) fields.add(new Field("Mission", m.getName()));


        return fields;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Crew member deleted
        if (event.getSource() instanceof Kerbal kerbal) {
            crew.remove(kerbal.getName());
            crewObjs.remove(kerbal);
        }

        // Concept deleted
        if (event.getSource() instanceof Concept) {
            concept = "[REDACTED]";
            conceptObj = null;
        }

        // Mission deleted
        if (event.getSource() instanceof Mission mission) {
            missions.remove(mission.getName());
            missionObjs.remove(mission);
        }

        // Connected vessel deleted
        if (event.getSource() instanceof Vessel vessel) {
            vessels.remove(vessel.getId());
            vesselObjs.remove(vessel);
        }
    }

    @Override
    public String toString() {
        return concept + " Mk" + getIteration() + ": " + status.toString() + ", " + location.toString();
    }
}
