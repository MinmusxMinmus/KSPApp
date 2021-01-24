package kerbals;

import java.util.Locale;

public enum Role {
    PILOT, SCIENTIST, ENGINEER;

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase(Locale.ROOT) + name().substring(1).toLowerCase(Locale.ROOT);
    }
}
