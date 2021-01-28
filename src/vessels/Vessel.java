package vessels;

import other.ControllerInterface;
import other.KSPObject;

import java.util.Collection;
import java.util.LinkedList;

public abstract class Vessel extends KSPObject {

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
    public Vessel(ControllerInterface controllerInterface, VesselType type, int iteration) {
        super(controllerInterface);
        this.type = type;
        this.iteration = iteration;
    }

    public abstract String getName();

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(type.name());
        ret.add(Integer.toString(iteration));

        return ret;
    }

    public VesselType getType() {
        return type;
    }

    public int getIteration() {
        return iteration;
    }

    public void newIteration() {
        iteration++;
    }
}

