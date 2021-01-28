package kerbals;

import java.util.Locale;

public enum Job {
    PILOT, SCIENTIST, ENGINEER;

    public static Job fromString(String s) {
        return switch (s.toLowerCase(Locale.ROOT).substring(0, 1)) {
            case "p" -> PILOT;
            case "s" -> SCIENTIST;
            case "e" -> ENGINEER;
            default -> PILOT;
        };
    }

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase(Locale.ROOT) + name().substring(1).toLowerCase(Locale.ROOT);
    }
}
