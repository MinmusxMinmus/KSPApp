package vessels;

import other.KSPObject;
import controller.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Destination;
import other.util.Field;
import other.util.KSPDate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Concepts represent different vessel designs from the VAB/SPH. These designs will eventually (hopefully) be consumated
 * into new {@link Vessel} objects.
 * <p>
 *     Concept fields include the vessel family name, the current iteration, the creation date of the original concept,
 *     the original concept that the current one was originally based on, and collections of properties
 *     ({@link VesselProperty}), approved destinations ({@link Destination}), and iteration changes
 *     ({@link IterationChange}).
 * </p>
 */
public class Concept extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 9; // ALWAYS ACCOUNT FOR DESCRIPTION, IN THIS CASE PARENT CLASS TOO

    public static final String DELIMITER = ":VC:";

    // Persistent fields
    /**
     * Vessel name. Must not include any modifiers, just the ship name.
     */
    private String name;
    /**
     * Vessel purpose. Roughly indicates the way a vessel is intended to work.
     */
    private VesselType type;
    /**
     * Concept iteration, roughly indicates the amount of change between the same family of ships.
     */
    private int iteration = 0;
    /**
     * Parent vessel, from which the current concept is inspired on. A null value indicates a brand new concept.
     */
    private String concept;
    /**
     * List containing the different changes in the concept's iteration history.
     */
    private final List<IterationChange> iterations;
    /**
     * Aditional properties the vessel may or may not have. Examples include VTOL capability, SSRT, etc.
     */
    private final Set<VesselProperty> properties;
    /**
     * Vessel's designed itinerary. Represented by the different locations the ship may find itself in
     */
    private final Set<Destination> destinations;
    private final KSPDate creationDate;

    // Dynamic fields
    private Concept conceptObj;

    // Constructors
    /** Generates a new concept from scratch, which means that it's not based off of any previous craft.
     * @param name The name of the new vessel family.
     * @param type The general purpose of the vessel.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    public Concept(ControllerInterface controller, String name, VesselType type, KSPDate creationDate, Destination[] destinations, VesselProperty... properties) {
        super(controller);
        this.type = type;
        this.iteration = 1;
        this.name = name;
        this.concept = "None";
        this.iterations = new LinkedList<>();
        this.properties = Arrays.stream(properties).collect(Collectors.toSet());
        this.destinations = Arrays.stream(destinations).collect(Collectors.toSet());
        this.creationDate = creationDate;
    }

    /** Generates a new concept based on an existing model.
     * @param vessel The vessel family it will be based on.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    public Concept(ControllerInterface controller, String name, Concept vessel, KSPDate creationDate, Destination[] destinations, VesselProperty... properties) {
        super(controller);
        this.type = vessel.getType();
        this.iteration = 1;
        this.name = name;
        this.concept = vessel.getName();
        this.iterations = new LinkedList<>();
        this.properties = Arrays.stream(properties).collect(Collectors.toSet());
        this.destinations = Arrays.stream(destinations).collect(Collectors.toSet());
        this.creationDate = creationDate;
    }

    /** Generates a concept from a list of fields stored in persistence.
     * @param fields List of the vessel's fields
     */
    public Concept(ControllerInterface controller, LinkedList<String> fields) {
        super(controller);
        setDescription(fields.get(0));
        this.type = VesselType.valueOf(fields.get(1));
        this.iteration = Integer.parseInt(fields.get(2));
        this.name = fields.get(3);
        this.concept = fields.get(4);
        this.iterations = changesFromString(controller, fields.get(5));
        this.properties = propertiesFromString(fields.get(6));
        this.destinations = destinationsFromString(fields.get(7));
        this.creationDate = KSPDate.fromString(controller, fields.get(8));
    }

    private static List<IterationChange> changesFromString(ControllerInterface controller, String s) {
        if (s.equals("(none)")) return new LinkedList<>();
        return Arrays.stream(s.split(DELIMITER)).map(ss -> IterationChange.fromString(controller, ss)).collect(Collectors.toList());
    }

    private static Set<Destination> destinationsFromString(String s) {
        return Arrays.stream(s.split(DELIMITER)).map(Destination::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    private static Set<VesselProperty> propertiesFromString(String s) {
        if (s.equals("(none)")) return new HashSet<>();
        return Arrays.stream(s.split(DELIMITER)).map(VesselProperty::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    // Logic methods
    /** Retires the current generation, replacing it with the next version.
     * @param changes Changes to the previous version.
     */
    public void newIteration(IterationChange changes) {
        iteration++;
        iterations.add(changes);
        // TODO Use changes to modify properties
    }

    // Getter/Setter methods
    public String getName() {
        return name;
    }

    public VesselType getType() {
        return type;
    }

    public int getIteration() {
        return iteration;
    }

    public String getConcept() {
        return concept;
    }

    public List<IterationChange> getIterations() {
        return Collections.unmodifiableList(iterations);
    }

    public Set<VesselProperty> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    public Set<Destination> getDestinations() {
        return Collections.unmodifiableSet(destinations);
    }

    // Overrides
    @Override
    public void ready() {
        conceptObj = getController().getConcept(concept);
        if (conceptObj != null) conceptObj.addEventListener(this);
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", name));
        fields.add(new Field("Iteration", "Mk" + iteration));
        fields.add(new Field("Type", type.toString()));
        fields.add(new Field("Parent design", concept == null ? "None" : concept));
        for (VesselProperty property : properties) fields.add(new Field("Property", property.toString()));
        for (Destination d : destinations) fields.add(new Field("Designed to work on:", d.toString()));

        return fields;
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(type.name());
        ret.add(Integer.toString(iteration));
        ret.add(name);
        ret.add(concept == null ? "(none)" : concept);
        StringJoiner joiner1 = new StringJoiner(DELIMITER);
        iterations.forEach(ic -> joiner1.add(IterationChange.toString(ic)));
        ret.add(joiner1.toString().equals("") ? "(none)" : joiner1.toString());
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (VesselProperty p : properties) joiner.add(p.name());
        ret.add(joiner.toString().equals("") ? "(none)" : joiner.toString());
        StringJoiner joiner2 = new StringJoiner(DELIMITER);
        for (Destination l : destinations) joiner2.add(l.name());
        ret.add(joiner2.toString());
        ret.add(creationDate.toStorableString());

        return ret;
    }

    @Override
    public boolean isComplexField(int index) {
        return index == 3;
    }

    @Override
    public KSPObject getComplexField(int index) {
        if (index == 3) return conceptObj;
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
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Deleted parent concept
        if (event.getSource() instanceof Concept) {
            conceptObj = null;
            concept = "[REDACTED]";
        }
    }

    @Override
    public String toString() {
        return name + " Mk" + iteration;
    }
}
