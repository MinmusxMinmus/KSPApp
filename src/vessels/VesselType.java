package vessels;

import java.util.Locale;

public enum VesselType {
    STATION, LANDER, ROVER, SPACECRAFT, WATERCRAFT, OTHER, UNKNOWN;

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase(Locale.ROOT) + name().substring(1).toLowerCase(Locale.ROOT);
    }
}
