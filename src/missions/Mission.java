package missions;

import kerbals.Kerbal;
import vessels.Vessel;

import java.util.*;

public class Mission {

    private final String name;
    private final String description;
    private final Vessel vessel;
    private Set<Kerbal> crew;
    private final List<MissionEvent> eventLog = new LinkedList<>();

    public Mission(String name, String description, Vessel vessel, Set<Kerbal> crew) {
        this.name = name;
        this.description = description;
        this.vessel = vessel;
        this.crew = crew;
    }

    public void logEvent(MissionEvent event) {
        eventLog.add(event);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Vessel getVessel() {
        return vessel;
    }
}
