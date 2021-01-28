package vessels;

import kerbals.Kerbal;
import missions.Mission;
import other.CelestialBody;
import other.ControllerInterface;
import other.Field;

import java.util.*;
import java.util.stream.Collectors;

public class VesselInstance extends Vessel {

    public static final int ENCODE_FIELD_AMOUNT = 10; // ALWAYS ACCOUNT FOR DESCRIPTION (IN THIS CASE, FOR TYPE AND ITERATION AS WELL)
    public static final String DELIMITER = ":VI:";

    // Persistent fields
    private final long id;
    private final String concept;
    private boolean inSpace;
    private CelestialBody location;
    private final Set<String> crew;
    private boolean crashed;
    private String crashDetails;

    // Dynamic fields
    private Mission mission;
    private final Set<Kerbal> crewMembers = new HashSet<>();

    /** Defines a new instance of the vessel concept, at the rough location specified.
     * @param id Vessel identifier
     * @param concept Vessel design
     * @param inSpace True if the craft is in space, false otherwise (surface of a celestial body, atmosphere, etc)
     * @param location Celestial body the craft is located at.
     */
    public VesselInstance(ControllerInterface controller, long id, VesselConcept concept, boolean inSpace, CelestialBody location, Kerbal... crew) {
        this(controller,
                concept.getType(),
                concept.getIteration(),
                id,
                concept.getName(),
                inSpace,
                location,
                Arrays.stream(crew).filter(Objects::nonNull).map(Kerbal::getName).collect(Collectors.toSet()),
                false,
                null);
    }

    /** Defines a new instance of the vessel concept, as a craft launching from one of Kerbin's launch sites.
     * @param concept Vessel design
     * @param id Vessel identifier
     */
    public VesselInstance(ControllerInterface controller, VesselConcept concept, long id, Kerbal... crew) {
        this(controller,
                id,
                concept,
                false,
                CelestialBody.KERBIN,
                crew);
    }

    /** Private implementation. Add params later
     */
    private VesselInstance(ControllerInterface controller, VesselType type, int iteration, long id, String concept, boolean inSpace, CelestialBody location, Set<String> crew, boolean crashed, String crashDetails) {
        super(controller, type, iteration);
        this.id = id;
        this.concept = concept;
        this.inSpace = inSpace;
        this.location = location;
        this.crew = crew;
        this.crashed = crashed;
        this.crashDetails = crashDetails;
    }

    /** Generates a new vessel instance from a list of fields stored in persistence.
     * @param fields List of fields
     */
    public VesselInstance(ControllerInterface controller, List<String> fields) {
        this(controller,
                VesselType.valueOf(fields.get(1)),
                Integer.parseInt(fields.get(2)),
                Long.parseLong(fields.get(3)),
                fields.get(4),
                Boolean.parseBoolean(fields.get(5)),
                CelestialBody.valueOf(fields.get(6)),
                crewMembersFromString(fields.get(7)),
                Boolean.parseBoolean(fields.get(8)),
                fields.get(9).equals("(none)") ? null : fields.get(9)
        );
        setDescription(fields.get(0));
    }

    private static Set<String> crewMembersFromString(String s) {
        return s.equals("(none)") ? new HashSet<>(): Arrays.stream(s.split(DELIMITER)).collect(Collectors.toSet());
    }

    private String crewMembersToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        crew.forEach(joiner::add);
        if (joiner.toString().equals("")) joiner.add("(none)");
        return joiner.toString();
    }



    /** Executed whenever a vessel is recovered. This method assumes that the vessel is already in Kerbin surface, and
     * recovers all kerbals inside it.
     */
    public void recover() {
        // Mission end
        this.mission = null;
        if (inSpace && location != CelestialBody.KERBIN) System.err.println("WARNING: Recovering vessel while not in Kerbin surface! Vessel: " + getName() + ", location " + location + ", in space? " + inSpace);

        // Crew recovery
        for (Kerbal k : crewMembers) {
            k.recover();
            crew.remove(k.getName());
        }

        // Recovery double check
        if (!crewMembers.isEmpty()) System.err.println("WARNING: Vessel was unable to recover all members! Remaining members: " + crewMembers.size());
        if (!crew.isEmpty()) System.err.println("WARNING: Vessel was unable to recover all member names! Remaining member names: " + crew.size());

        // Recover the current vessel
        getController().instanceRecovered(this);
    }

    /** Indicates that the vessel has been destroyed, either by crash or by Tracking Station. Kills all crew inside it.
     * Since only {@link Mission} can call it, it guarantees that the vessel has a mission.
     * @param details Destruction details
     */
    public void destroyed(String details) {
        // Crew deaths
        for (Kerbal k : crewMembers) {
            float expGained = k.getExpGainedFromCurrentMission();
            k.KIA(k.getCurrentMission(), inSpace, location, expGained, "Died on " + k.getCurrentMission().getName() + ", during destruction of " + getName());
        }
        crewMembers.clear();

        // Vessel destruction
        crashed = true;
        crashDetails = details;
        getController().instanceCrashed(this);
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

    public Set<String> getCrew() {
        return new HashSet<>(crew);
    }

    public CelestialBody getLocation() {
        return location;
    }

    public void setInSpace(boolean inSpace) {
        this.inSpace = inSpace;
    }

    public void setLocation(CelestialBody location) {
        this.location = location;
    }


    @Override
    public void ready() {
        // Get vessel mission
        Set<Mission> missions = getController().getMissions();
        this.mission = missions.stream().filter(m -> m.getVesselId() == id).findFirst().orElse(null);
        // Get crew
        for (String s : crew) {
            crewMembers.add(getController().getKerbal(s));
        }
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(Long.toString(id));
        ret.add(concept);
        ret.add(Boolean.toString(inSpace));
        ret.add(location.name());
        ret.add(crewMembersToString());
        ret.add(Boolean.toString(crashed));
        ret.add(crashDetails == null ? "(none)" : crashDetails);

        return ret;
    }

    @Override
    public String getName() {
        return concept;
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
        if (crashed) {
            fields.add(new Field("Last mission", mission == null ? "None" : mission.getName()));
            fields.add(new Field("Crash location", (inSpace ? "Orbit of " : "") + location.toString()));
            fields.add(new Field("Crash details", crashDetails));
        } else {
            fields.add(new Field("Mission", mission == null ? "None" : mission.getName()));
            fields.add(new Field("Type", getType().toString()));
            fields.add(new Field("Location", inSpace ? "Orbiting " : "Landed on " + location.toString()));
        }
        for (String s : crew) fields.add(new Field("Crew member", s + " Kerman" + (crashed ? " (KIA)" : "")));


        return fields;
    }
}
