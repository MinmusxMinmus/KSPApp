package other;

import vessels.Vessel;
import vessels.VesselConcept;
import vessels.VesselInstance;
import vessels.VesselType;

import java.util.Comparator;

/**
 * Comparator class for the {@link Vessel} hierarchy.
 * <br>
 *     While a vessel concept can be the same as another one, different vessel instances cannot be the same vessel.
 *     Hence, the comparator cannot return 0 when comparing two instances.
 */
public class VesselNaturalComparator implements Comparator<Vessel> {
    @Override
    public int compare(Vessel v1, Vessel v2) {
        // Case 1: two concepts
        if (v1 instanceof VesselConcept vc1 && v2 instanceof VesselConcept vc2) {
            // Different family
            if (!vc1.getName().equals(vc2.getName())) return vc1.getName().compareTo(vc2.getName());
            // Same family, refer to iteration
            return vc2.getIteration() - vc1.getIteration();
        }

        // Case 2: two instances
        if (v1 instanceof VesselInstance vi1 && v2 instanceof VesselInstance vi2) {
            // Different family
            if (!vi1.getName().equals(vi2.getName())) return vi1.getName().compareTo(vi2.getName());
            // Same family, refer to iteration
            if (vi1.getIteration() != vi2.getIteration()) return vi2.getIteration() - vi1.getIteration();
            // Same iteration, refer to location
            if (!vi1.getLocation().equals(vi2.getLocation())) return vi1.getLocation().compareTo(vi2.getLocation());
            // Same location, refer to specific location
            if (vi1.isInSpace() != vi2.isInSpace()) return vi1.isInSpace() ? 1 : -1; // Vessels in space take priority
            // Same specific location, apply random order
            return 1;
        }

        // Case 3: first vessel is an instance
        if (v1 instanceof VesselInstance vi1) return compare(vi1.getConcept(), v2);

        // Case 4: second vessel is an instance
        return compare(v1, ((VesselInstance)v2).getConcept());
    }
}
