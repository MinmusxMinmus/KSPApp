package controller;

import kerbals.Kerbal;
import kerbals.Role;
import missions.Mission;
import other.KSPDate;
import other.VesselNaturalComparator;
import vessels.*;

import java.util.*;

public class GUIController {

    private final List<Kerbal> kerbals;
    private final List<Mission> missions;
    private final List<VesselConcept> concepts;
    private final List<VesselInstance> instances;

    public GUIController() {
        this.kerbals = new LinkedList<>();
        this.missions = new LinkedList<>();
        this.concepts = new LinkedList<>();
        this.instances = new LinkedList<>();

        // TODO read from persistency
    }


    public List<Kerbal> getKerbals() {
        return Collections.unmodifiableList(kerbals);
    }

    public List<Mission> getMissions() {
        return Collections.unmodifiableList(missions);
    }

    public List<VesselConcept> getVesselConcepts() {
        return Collections.unmodifiableList(concepts);
    }

    public VesselConcept getVesselConcept(String name) {
        concepts.sort(new VesselNaturalComparator());
        return concepts.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    public List<VesselInstance> getVesselInstances() {
        instances.sort(new VesselNaturalComparator());
        return Collections.unmodifiableList(instances);
    }

    public VesselInstance getVesselInstance(String name) {
        return instances.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    public List<Vessel> getAllVessels() {
        List<Vessel> ret = new LinkedList<>();
        ret.addAll(concepts);
        ret.addAll(instances);
        ret.sort(new VesselNaturalComparator());
        return Collections.unmodifiableList(ret);
    }

    public void addKerbalHired(String name, boolean isMale, Role role, KSPDate hiringDate) {
        kerbals.add(new Kerbal(name, isMale, role, hiringDate));
    }

    public void addKerbalRescued(String name, boolean isMale, Role role, Mission origin, KSPDate rescueDate) {
        Kerbal rescued = new Kerbal(name, isMale, role, rescueDate);
        kerbals.add(rescued);
        origin.kerbalRescued(rescued, rescueDate);
    }

    public void addMission(String name, String description, Vessel vessel, Map<Kerbal, String> crew, KSPDate missionStart) {
        missions.add(new Mission(name, description, vessel, crew, missionStart));
        markAssigned(crew.keySet());
    }

    public void addVesselConcept(String name, VesselType type, VesselDestination[] destinations, VesselProperty... properties) {
        concepts.add(new VesselConcept(name, type, destinations, properties));
    }

    public void addVesselInstance(VesselConcept concept) {
        for (VesselConcept v : concepts) {
            if (v.getName().equals(concept.getName()) && v.getIteration() == concept.getIteration()) {
                instances.add(new VesselInstance(concept));
                break;
            }
        }
    }

    public void markAssigned(Set<Kerbal> kerbalSet) {
        for (Kerbal k : kerbalSet) k.setAvailable(false);
    }
}
