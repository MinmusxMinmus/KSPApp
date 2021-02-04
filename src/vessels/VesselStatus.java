package vessels;

import java.util.Locale;

public enum VesselStatus {
    NOMINAL, STRANDED, CRASHED;


    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT);
    }
}
