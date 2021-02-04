package other.util;

import java.util.StringJoiner;

/**
 * The Location class defines a rough point in the Kerbol system. The location of an object can be described by:<br>
 * - The celestial body the object is orbiting, and<br>
 * - If the object is in space, or not<br>
 * With these two points of reference a location can be roughly determined, and consequently used for rendezvous
 * checks and other relative position calculations.<br>
 * <p>Notes: in the future, a Location object could include specific details such as orbit periapsis/apoapsis, orbit
 *  * eccentricity, biome landed, water landing, and so on. This would in turn allow for more specific calculations, such
 *  * as Delta-V requirements.</p>
 */
public class Location {

    private static final String DELIMITER = ":L:";
    private static final int ENCODE_FIELD_AMOUNT = 2;

    /**
     * Whether a vessel is in space or not, duhh.
     */
    private final boolean inSpace;
    /**
     * The {@link CelestialBody} the vessel is currently at.
     */
    private final CelestialBody celestialBody;

    /**
     * Generates an inmutable location object.
     */
    public Location(boolean inSpace, CelestialBody celestialBody) {
        this.inSpace = inSpace;
        this.celestialBody = celestialBody;
    }

    /**
     * Generates a storable string from the given location.
     */
    public static String toString(Location location) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(Boolean.toString(location.inSpace));
        joiner.add(location.celestialBody.name());


        return joiner.toString();
    }

    /**
     * Generates a Location object from a storable string.
     */
    public static Location fromString(String s) {
        String[] fields = s.split(DELIMITER);
        if (fields.length != ENCODE_FIELD_AMOUNT) return null;
        return new Location(
                Boolean.parseBoolean(fields[0]),
                CelestialBody.valueOf(fields[1])
        );
    }


    /**
     * Returns whether the craft is currently landed at the given {@link CelestialBody}.
     * @return {@code true} if the object is landed, {@code false} otherwise.
     */
    public boolean landedAt(CelestialBody celestialBody) {
        return !inSpace && this.celestialBody.equals(celestialBody);
    }

    /**
     * Returns whether the craft is currently orniting the given {@link CelestialBody}.
     * @return {@code true} if the object is in orbit, {@code false} otherwise.
     */
    public boolean orbiting(CelestialBody celestialBody) {
        return !landedAt(celestialBody);
    }

    public boolean isInSpace() {
        return inSpace;
    }

    public CelestialBody getCelestialBody() {
        return celestialBody;
    }

    /**
     * Returns whether the current craft is in contact with an atmosphere.
     */
    public boolean inAtmosphere() {
        return !inSpace && celestialBody.hasAtmosphere();
    }

    @Override
    public String toString() {
        return (inSpace ? "Orbiting " : "Landed on ") + celestialBody.toString();
    }
}
