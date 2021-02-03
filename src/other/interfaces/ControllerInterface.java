package other.interfaces;

import kerbals.Kerbal;
import missions.Mission;
import vessels.Concept;
import vessels.Vessel;

import java.util.Set;

public interface ControllerInterface {
    Kerbal getKerbal(String name);
    Set<Kerbal> getKerbals();

    Mission getMission(String name);
    Set<Mission> getMissions();

    Concept getConcept(String name);
    Set<Concept> getConcepts();

    Vessel getInstance(long id);
    Set<Vessel> getInstances();

    Vessel getCrashedInstance(long id);
    Set<Vessel> getCrashedInstances();

    void addKerbal(Kerbal kerbal);
    void addMission(Mission mission);
    void addConcept(Concept concept);
    void addInstance(Vessel instance);

    // Special case: recovered instances get deleted from memory
    void instanceRecovered(Vessel instance);

    // Special case: crashed instances get moved to their own list
    void instanceCrashed(Vessel vessel);
}
