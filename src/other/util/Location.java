package other.util;

public class Location {

    private static final String DELIMITER = ":L:";
    private static final int ENCODE_FIELD_AMOUNT = 2;

    private final boolean inSpace;
    private final CelestialBody celestialBody;

    public Location(boolean inSpace, CelestialBody celestialBody) {
        this.inSpace = inSpace;
        this.celestialBody = celestialBody;
    }

    public static String toString(Location l) {
        return l.inSpace + DELIMITER + l.celestialBody.name();
    }

    public static Location fromString(String s) {
        String[] split = s.split(DELIMITER);
        if (split.length != ENCODE_FIELD_AMOUNT) return null;
        return new Location(Boolean.parseBoolean(split[0]), CelestialBody.valueOf(split[1]));
    }


    public boolean landedAt(CelestialBody celestialBody) {
        return !inSpace && this.celestialBody.equals(celestialBody);
    }

    public boolean orbiting(CelestialBody celestialBody) {
        return !landedAt(celestialBody);
    }

    public boolean isInSpace() {
        return inSpace;
    }

    public CelestialBody getCelestialBody() {
        return celestialBody;
    }

    @Override
    public String toString() {
        return (inSpace ? "Orbiting " : "Landed on ") + celestialBody.toString();
    }
}
