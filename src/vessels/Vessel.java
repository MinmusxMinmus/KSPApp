package vessels;

import other.KSPObject;

import java.util.Collection;
import java.util.LinkedList;

public abstract class Vessel extends KSPObject {

    public static final String CONCEPT_STRING = "VesselConcept";
    public static final String INSTANCE_STRING = "VesselInstance";

    /**
     * Vessel purpose. Roughly indicates the way a vessel is intended to work.
     */
    private final VesselType type;
    /**
     * Concept iteration, roughly indicates the amount of change between the same family of ships.
     */
    private int iteration = 0;

    /** New vessel from scratch.
     * @param type Rough description of the vessel's purpose
     */
    public Vessel(VesselType type, int iteration) {
        this.type = type;
        this.iteration = iteration;
    }

    public static String[] getFieldNames() {
        return new String[]{"Name", };
    }


    public VesselType getType() {
        return type;
    }

    public abstract String getName();

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>();
        ret.add(this.getClass().getSimpleName());
        ret.add(new LinkedList<>(super.toStorableCollection()).get(0));
        return ret;
    }

    public int getIteration() {
        return iteration;
    }

    public void newIteration() {
        iteration++;
    }
}

