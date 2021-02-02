package other.interfaces;

import kerbals.Kerbal;
import missions.Mission;
import vessels.VesselConcept;
import vessels.VesselInstance;

import java.util.List;
import java.util.Set;

public interface ControllerInterface {
    Kerbal getKerbal(String name);
    Set<Kerbal> getKerbals();

    Mission getMission(String name);
    Set<Mission> getMissions();

    VesselConcept getConcept(String name);
    Set<VesselConcept> getConcepts();

    VesselInstance getInstance(long id);
    Set<VesselInstance> getInstances();

    VesselInstance getCrashedInstance(long id);
    Set<VesselInstance> getCrashedInstances();

    void addKerbal(Kerbal kerbal);
    void addMission(Mission mission);
    void addConcept(VesselConcept concept);
    void addInstance(VesselInstance instance);

    // Special case: recovered instances get deleted from memory
    void instanceRecovered(VesselInstance instance);

    // Special case: crashed instances get moved to their own list
    void instanceCrashed(VesselInstance vesselInstance);
}
