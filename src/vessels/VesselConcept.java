package vessels;

import java.util.*;

public class VesselConcept extends Vessel {

    /**
     * Concept iteration, roughly indicates the amount of change between the same family of ships.
     */
    private int iteration = 0;
    /**
     * Parent vessel, from which the current concept is inspired on. A null value indicates a brand new concept.
     */
    private final Vessel parentConcept;
    /**
     * List containing the different changes in the concept's iteration history.
     */
    private final List<IterationChange> iterationChanges = new LinkedList<>();
    /**
     * Aditional properties the vessel may or may not have. Examples include VTOL capability, SSRT, etc.
     */
    private final Set<VesselProperty> properties = new HashSet<>();
    /**
     * Vessel's designed itinerary. Represented by the different locations the ship may find itself in
     */
    private final Set<VesselDestination> destinations = new HashSet<>();

    private String notes;


    /** Generates a new concept from scratch, which means that it's not based off of any previous craft.
     * @param name The name of the new vessel family.
     * @param type The general purpose of the vessel.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    public VesselConcept(String name,
                         VesselType type,
                         VesselDestination[] destinations,
                         VesselProperty... properties) {
        this(name, type, null, destinations, properties);
    }

    /** Generates a new concept based on an existing model.
     * @param vessel The vessel family it will be based on.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    public VesselConcept(VesselConcept vessel,
                         VesselDestination[] destinations,
                         VesselProperty... properties) {
        this(vessel.getName(), vessel.getType(), vessel, destinations, properties);
    }

    /** Private constructor implementation.
     * @param name The name of the new vessel family.
     * @param type The general purpose of the vessel.
     * @param parentConcept The vessel family it will be based on.
     * @param destinations Vessel's designed itinerary.
     * @param properties List with various optional properties the vessel might have.
     */
    private VesselConcept(String name,
                          VesselType type,
                          VesselConcept parentConcept,
                          VesselDestination[] destinations,
                          VesselProperty... properties) {
        super(name, type);
        this.parentConcept = parentConcept;
        Collections.addAll(this.properties, properties);
        Collections.addAll(this.destinations, destinations);
    }


    /** Retires the current generation, replacing it with the next version.
     * @param changes Changes to the previous version.
     */
    public void newIteration(IterationChange changes) {
        iteration++;
        iterationChanges.add(changes);
        // TODO Use changes to modify properties
    }

    public Vessel getParentConcept() {
        return parentConcept;
    }

    public int getIteration() {
        return iteration;
    }

    public List<IterationChange> getIterationChanges() {
        return Collections.unmodifiableList(iterationChanges);
    }

    public Set<VesselProperty> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    public Set<VesselDestination> getDestinations() {
        return Collections.unmodifiableSet(destinations);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!this.getClass().equals(obj.getClass())) return false;
        VesselConcept vc = (VesselConcept) obj;
        return super.equals(vc) && this.iteration == vc.iteration;
    }

    @Override
    public String getTextRepresentation() {
        return super.getTextRepresentation() + " Mk" + (iteration + 1);
    }

    @Override
    public int getFieldCount() {
        return super.getFieldCount() + 2;
    }

    @Override
    public String getFieldName(int index) {
        if (index < super.getFieldCount()) return super.getFieldName(index);
        return switch (index) {
            case 2 -> "Iteration";
            case 3 -> "Parent design";
            default -> null;
        };
    }

    @Override
    public String getFieldValue(int index) {
        if (index < super.getFieldCount()) return super.getFieldName(index);
        return switch (index) {
            case 2 -> "Mk" + iteration;
            case 3 -> (parentConcept == null) ? "(None)" : parentConcept.getName();
            default -> null;
        };
    }
}
