package vessels;

import kerbals.Kerbal;
import other.CelestialBody;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VesselInstance extends Vessel {

    private final int iteration;
    private final VesselConcept concept;
    private boolean inSpace;
    private CelestialBody location;
    private final Set<Kerbal> crew = new HashSet<>();

    private String notes;

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

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public int getFieldCount() {
        return super.getFieldCount() + 3;
    }

    @Override
    public String getFieldName(int index) {
        if (index < super.getFieldCount()) return super.getFieldName(index);
        return switch (index) {
            case 2 -> "Iteration";
            case 3 -> "Status";
            case 4 -> "Location";
            default -> null;
        };
    }

    @Override
    public String getFieldValue(int index) {
        if (index < super.getFieldCount()) return super.getFieldName(index);
        return switch (index) {
            case 2 -> "Mk" + iteration;
            case 4 -> inSpace ? "In orbit" : "Surface landed";
            case 5 -> location.toString();
            default -> null;
        };
    }
}
