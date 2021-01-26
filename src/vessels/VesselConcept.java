package vessels;

import java.util.*;
import java.util.stream.Collectors;

public class VesselConcept extends Vessel {

    public static final int ENCODE_FIELD_AMOUNT = 8; // ALWAYS ACCOUNT FOR DESCRIPTION, IN THIS CASE PARENT CLASS TOO

    public static final String DELIMITER = ":VC:";

    /**
     * Vessel name. Must not include any modifiers, just the ship name.
     */
    private final String name;
    /**
     * Parent vessel, from which the current concept is inspired on. A null value indicates a brand new concept.
     */
    private final String parentConcept;
    /**
     * List containing the different changes in the concept's iteration history.
     */
    private final List<IterationChange> iterationChanges;
    /**
     * Aditional properties the vessel may or may not have. Examples include VTOL capability, SSRT, etc.
     */
    private final Set<VesselProperty> properties;
    /**
     * Vessel's designed itinerary. Represented by the different locations the ship may find itself in
     */
    private final Set<Destination> destinations;


    /** Generates a new concept from scratch, which means that it's not based off of any previous craft.
     * @param name The name of the new vessel family.
     * @param type The general purpose of the vessel.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    public VesselConcept(String name,
                         VesselType type,
                         Destination[] destinations,
                         VesselProperty... properties) {
        this(type,
                0,
                name,
                null,
                new LinkedList<>(),
                Arrays.stream(properties).collect(Collectors.toUnmodifiableSet()),
                Arrays.stream(destinations).collect(Collectors.toUnmodifiableSet()));
    }

    /** Generates a new concept based on an existing model.
     * @param vessel The vessel family it will be based on.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    public VesselConcept(String name,
                         VesselConcept vessel,
                         Destination[] destinations,
                         VesselProperty... properties) {
        this(vessel.getType(),
                0,
                name,
                vessel.getName(),
                new LinkedList<>(),
                Arrays.stream(properties).collect(Collectors.toUnmodifiableSet()),
                Arrays.stream(destinations).collect(Collectors.toUnmodifiableSet()));
    }

    /** Private constructor implementation.
     * @param name The name of the new vessel family.
     * @param type The general purpose of the vessel.
     * @param parentConcept The vessel family it will be based on.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    private VesselConcept(VesselType type,
                          int iteration,
                          String name,
                          String parentConcept,
                          List<IterationChange> iterationChanges,
                          Set<VesselProperty> properties,
                          Set<Destination> destinations) {
        super(type, iteration);
        this.name = name;
        this.parentConcept = parentConcept;
        this.iterationChanges = iterationChanges;
        this.properties = properties;
        this.destinations = destinations;
    }

    /** Generates a concept from a list of fields stored in persistence.
     * @param fields List of the vessel's fields
     */
    public VesselConcept(LinkedList<String> fields) {
        this(VesselType.valueOf(fields.get(1)),
                Integer.parseInt(fields.get(2)),
                fields.get(3),
                fields.get(4).equals("(none)") ? null : fields.get(4),
                changesFromString(fields.get(5)),
                propertiesFromString(fields.get(6)),
                destinationsFromString(fields.get(7))
        );
        setDescription(fields.get(0));
    }

    private static List<IterationChange> changesFromString(String s) {
        if (s.equals("(none)")) return new LinkedList<>();
        return Arrays.stream(s.split(DELIMITER)).map(IterationChange::fromString).collect(Collectors.toList());
    }

    private String changesToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        iterationChanges.forEach(ic -> joiner.add(IterationChange.toString(ic)));
        return joiner.toString().equals("") ? "(none)" : joiner.toString();
    }

    private static Set<Destination> destinationsFromString(String s) {
        return Arrays.stream(s.split(DELIMITER)).map(Destination::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    private String destinationsToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (Destination l : destinations) joiner.add(l.name());
        return joiner.toString();
    }

    private static Set<VesselProperty> propertiesFromString(String s) {
        if (s.equals("(none)")) return new HashSet<>();
        return Arrays.stream(s.split(DELIMITER)).map(VesselProperty::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    private String propertiesToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (VesselProperty p : properties) joiner.add(p.name());
        return joiner.toString().equals("") ? "(none)" : joiner.toString();
    }


    /** Retires the current generation, replacing it with the next version.
     * @param changes Changes to the previous version.
     */
    public void newIteration(IterationChange changes) {
        newIteration();
        iterationChanges.add(changes);
        // TODO Use changes to modify properties
    }

    public String getParentConcept() {
        return parentConcept;
    }

    public List<IterationChange> getIterationChanges() {
        return Collections.unmodifiableList(iterationChanges);
    }

    public Set<VesselProperty> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    public Set<Destination> getDestinations() {
        return Collections.unmodifiableSet(destinations);
    }

    @Override
    public String getTextRepresentation() {
        return getName() + " Mk" + (getIteration() + 1);
    }

    @Override
    public int getFieldCount() {
        return 4;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Name";
            case 1 -> "Iteration";
            case 2 -> "Type";
            case 3 -> "Parent design";
            default -> null;
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> name;
            case 1 -> "Mk" + getIteration();
            case 2 -> getType().toString();
            case 3 -> (parentConcept == null) ? "(None)" : parentConcept;
            default -> null;
        };
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(getType().name());
        ret.add(Integer.toString(getIteration()));
        ret.add(name);
        ret.add(parentConcept == null ? "(none)" : parentConcept);
        ret.add(changesToString());
        ret.add(propertiesToString());
        ret.add(destinationsToString());

        return ret;
    }

    public String getName() {
        return name;
    }
}
