package kerbals;

import kerbals.Kerbal;
import missions.Mission;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FlightLog {

    public final List<Mission> log = new LinkedList<>();
    public final Kerbal subject;

    public FlightLog(Kerbal kerbal) {
        this.subject = kerbal;
    }

    public void addEntry(Mission m) {
        log.add(m);
    }

    public List<Mission> getEntries() {
        return Collections.unmodifiableList(log);
    }

    // TODO Specific search methods
}
