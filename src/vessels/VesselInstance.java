package vessels;

import kerbals.Kerbal;
import other.CelestialBody;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VesselInstance extends Vessel {

    private final VesselConcept concept;
    private final int iteration;
    private boolean inSpace;
    private CelestialBody location;
    private final Set<Kerbal> crew = new HashSet<>();

    /** Defines a new instance of the vessel concept, at the rough location specified.
     * @param concept Vessel design
     * @param inSpace True if the craft is in space, false otherwise (surface of a celestial body, atmosphere, etc)
     * @param location Celestial body the craft is located at.
     */
    public VesselInstance(VesselConcept concept, boolean inSpace, CelestialBody location, Kerbal... crew) {
        super(concept.getName(), concept.getType());
        this.concept = concept;
        this.iteration = concept.getIteration();
        this.inSpace = inSpace;
        this.location = location;
        Collections.addAll(this.crew, crew);
    }

    /** Defines a new instance of the vessel concept, as a craft launching from one of Kerbin's launch sites.
     * @param concept Vessel design
     */
    public VesselInstance(VesselConcept concept) {
        this(concept, false, CelestialBody.KERBIN);
    }

    public VesselConcept getConcept() {
        return concept;
    }

    public int getIteration() {
        return iteration;
    }

    public boolean isInSpace() {
        return inSpace;
    }

    public Set<Kerbal> getCrew() {
        return Collections.unmodifiableSet(crew);
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
    public String getTextRepresentation() {
        return concept.getTextRepresentation() + (inSpace ? ": Orbiting ": ": Landed on ") + location.toString();
    }
}
