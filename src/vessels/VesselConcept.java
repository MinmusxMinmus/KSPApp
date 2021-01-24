package vessels;

import java.util.*;

public class VesselConcept extends Vessel {

    /**
     * Parent vessel, from which the current concept is inspired on. A null value indicates a brand new concept.
     */
    private final Vessel parentConcept;
    /**
     * Concept iteration, roughly indicates the amount of change between the same family of ships.
     */
    private int iteration = 0;
    /**
     * List containing the different changes in the concept's iteration history.
     */
    private final List<IterationChange> iterationChanges = new LinkedList<>();
    /**
     * Aditional properties the vessel may or may not have. Examples include VTOL capability, SSRT, etc.
     */
    private final Set<VesselProperties> properties = new HashSet<>();
    private String notes;


    /** Generates a new concept from scratch, which means that it's not based off of any previous craft.
     * @param name The name of the new vessel family.
     * @param type The general purpose of the vessel.
     * @param properties List with various optional properties the vessel might have.
     */
    public VesselConcept(String name, VesselType type, VesselProperties... properties) {
        this(name, type, null, properties);
    }

    /** Generates a new concept based on a different craft.
     * @param vessel The vessel family it will be based on.
     * @param properties List with various optional properties the vessel might have.
     */
    public VesselConcept(VesselConcept vessel, VesselProperties... properties) {
        this(vessel.getName(), vessel.getType(), vessel, properties);
    }

    private VesselConcept(String name, VesselType type, VesselConcept parentConcept, VesselProperties... properties) {
        super(name, type);
        this.parentConcept = parentConcept;
        Collections.addAll(this.properties, properties);
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

    public Set<VesselProperties> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return super.toString() + " Mk" + (iteration + 1);
    }
}
