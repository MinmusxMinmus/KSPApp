package other.util;

import java.util.Locale;

/**
 * Celestial bodies are the different "static" planets and moons present in the Kerbol system, as well as Kerbol itself.
 */
public enum CelestialBody {
    KERBIN, // Atmosphere: 70km
    THE_MUN,
    MINMUS,
    DUNA,
    IKE,
    EVE, // Atmosphere: 90km
    GILLY,
    MOHO,
    JOOL, // Atmosphere: 200km
    LAYTHE, // Atmosphere: 50km
    VALL,
    TYLO,
    BOP,
    POL,
    DRES,
    EELOO,
    KERBOL;

    public boolean hasAtmosphere() {
        return switch (this) {
            case KERBIN, DUNA, EVE, JOOL, LAYTHE -> true;
            default -> false;
        };
    }
    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase(Locale.ROOT) + name().substring(1).toLowerCase(Locale.ROOT).replaceAll("_", " ").replaceAll("The", "the");
    }
}
