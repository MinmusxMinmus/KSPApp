package other;

import java.util.Locale;

public enum CelestialBody {
    KERBIN, THE_MUN, MINMUS, DUNA, IKE, EVE, GILLY, MOHO, JOOL, LAYTHE, VALL, TYLO, BOP, POL, DRES, EELOO, KERBOL;

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase(Locale.ROOT) + name().substring(1).toLowerCase(Locale.ROOT).replaceAll("_", " ").replaceAll("The", "the");
    }
}
