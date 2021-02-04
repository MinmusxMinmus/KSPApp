package controller;

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
    Set<Vessel> getVessels();

    Vessel getCrashedInstance(long id);
    Set<Vessel> getCrashedVessels();

    void addKerbal(Kerbal kerbal);
    void addMission(Mission mission);
    void addConcept(Concept concept);
    void addVessel(Vessel vessel);

    // Special case: recovered instances get deleted from memory
    void vesselRecovered(Vessel vessel);

    // Special case: crashed instances get moved to their own list
    void vesselCrashed(Vessel vessel);

    void ready();
    long rng();
}
