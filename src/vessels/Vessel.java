package vessels;

import java.util.*;

public abstract class Vessel {

    /**
     * Vessel name. Must not include any modifiers, just the ship name.
     */
    private final String name;
    /**
     * Vessel purpose. Roughly indicates the way a vessel is intended to work.
     */
    private final VesselType type;

    /** New vessel from scratch.
     * @param name Vessel name.
     * @param type Rough description of the vessel's purpose
     */
    public Vessel(String name, VesselType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public VesselType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }
}

