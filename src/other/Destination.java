package other;

import java.util.Locale;

public enum Destination {
    KERBIN_LOCAL, KERBIN_LIMITED,
    MUN_LOCAL, MUN_LIMITED,
    MINMUS_LOCAL, MINMUS_LIMITED,
    DUNA_LOCAL, DUNA_LIMITED,
    IKE_LOCAL, IKE_LIMITED,
    EVE_LOCAL, EVE_LIMITED,
    GILLY_LOCAL, GILLY_LIMITED,
    JOOL_LOCAL, JOOL_LIMITED,
    LAYTHE_LOCAL, LAYTHE_LIMITED,
    VALL_LOCAL, VALL_LIMITED,
    TYLO_LOCAL, TYLO_LIMITED,
    BOP_LOCAL, BOP_LIMITED,
    POL_LOCAL, POL_LIMITED,
    DRES_LOCAL, DRES_LIMITED,
    EELOO_LOCAL, EELOO_LIMITED,
    KERBOL_LIMITED;

    public float recoveryMultiplier() {
        return switch (name().substring(0, 5)) {
            case "KERBI" -> 1.0f;
            case "MUN_L" -> 2.0f;
            case "MINMU" -> 2.5f;
            case "KERBO" -> 4.0f;
            case "EVE_L", "DUNA_", "IKE_L" -> 5.0f;
            case "GILLY", "DRES_", "JOOL_" -> 6.0f;
            case "MOHO_" -> 7.0f;
            case "LAYTH", "VALL_", "TYLO_", "BOP_L", "POL_L" -> 8.0f;
            case "EELOO" -> 10.0f;
            default -> 0.0f;
        };
    }


    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT).replaceAll("_", " ");
    }
}
