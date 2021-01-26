package vessels;

import kerbals.Kerbal;
import other.CelestialBody;

import java.util.*;
import java.util.stream.Collectors;

public class VesselInstance extends Vessel {

    public static final int ENCODE_FIELD_AMOUNT = 8; // ALWAYS ACCOUNT FOR DESCRIPTION (IN THIS CASE, FOR TYPE AS WELL)

    private final long id;
    private final String concept;
    private boolean inSpace;
    private CelestialBody location;
    private final Set<String> crew;

    /** Defines a new instance of the vessel concept, at the rough location specified.
     * @param id Vessel identifier
     * @param concept Vessel design
     * @param inSpace True if the craft is in space, false otherwise (surface of a celestial body, atmosphere, etc)
     * @param location Celestial body the craft is located at.
     */
    public VesselInstance(long id, VesselConcept concept, boolean inSpace, CelestialBody location, Kerbal... crew) {
        this(concept.getType(),
                id,
                concept.getIteration(),
                concept.getName(),
                inSpace,
                location,
                Arrays.stream(crew).filter(Objects::nonNull).map(Kerbal::getName).collect(Collectors.toSet()));
    }

    /** Defines a new instance of the vessel concept, as a craft launching from one of Kerbin's launch sites.
     * @param concept Vessel design
     * @param id Vessel identifier
     */
    public VesselInstance(VesselConcept concept, long id, Kerbal... crew) {
        this(id,
                concept,
                false,
                CelestialBody.KERBIN,
                crew);
    }

    /** Private implementation. Add params later
     */
    private VesselInstance(VesselType type, long id, int iteration, String concept, boolean inSpace, CelestialBody location, Set<String> crew) {
        super(type, iteration);
        this.id = id;
        this.concept = concept;
        this.inSpace = inSpace;
        this.location = location;
        this.crew = crew;
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(getType().name());
        ret.add(Long.toString(id));
        ret.add(Integer.toString(getIteration()));
        ret.add(concept);
        ret.add(Boolean.toString(inSpace));
        ret.add(location.name());
        ret.add(crewMembersToString());

        return ret;
    }

    /** Generates a new vessel instance from a list of fields stored in persistence.
     * @param fields List of fields
     */
    public VesselInstance(List<String> fields) {
        this(
                VesselType.valueOf(fields.get(1)),
                Long.parseLong(fields.get(2)),
                Integer.parseInt(fields.get(3)),
                fields.get(4),
                Boolean.parseBoolean(fields.get(5)),
                CelestialBody.valueOf(fields.get(6)),
                crewMembersFromString(fields.get(7))
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
    public String getName() {
        return concept;
    }

    @Override
    public String getTextRepresentation() {
        return concept + " Mk" + getIteration() + (inSpace ? ": Orbiting ": ": Landed on ") + location.toString();
    }

    @Override
    public int getFieldCount() {
        return 5;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Name";
            case 1 -> "Iteration";
            case 2 -> "Type";
            case 3 -> "Status";
            case 4 -> "Location";
            default -> null;
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> concept;
            case 1 -> "Mk" + getIteration();
            case 2 -> getType().toString();
            case 3 -> inSpace ? "In orbit" : "Surface landed";
            case 4 -> location.toString();
            default -> null;
        };
    }
}
