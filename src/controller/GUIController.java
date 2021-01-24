package controller;

import kerbals.Kerbal;
import kerbals.Role;
import missions.Mission;
import vessels.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GUIController {

    private final List<Kerbal> kerbals;
    private final List<Mission> missions;
    private final List<VesselConcept> vessels;
    private final List<VesselInstance> instances;

    public GUIController() {
        this.kerbals = new LinkedList<>();
        this.missions = new LinkedList<>();
        this.vessels = new LinkedList<>();
        this.instances = new LinkedList<>();

        // TODO read from persistency
    }


    public List<Kerbal> getKerbals() {
        return Collections.unmodifiableList(kerbals);
    }

    public List<Mission> getMissions() {
        return Collections.unmodifiableList(missions);
    }

    public List<Vessel> getVessels() {
        return Collections.unmodifiableList(vessels);
    }

    public List<VesselInstance> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public void addKerbal(String name, boolean isMale, Role role, int level) {
        kerbals.add(new Kerbal(name, isMale, role, level));
    }

    public void addKerbal(String name, boolean isMale, Role role) {
        addKerbal(name, isMale, role, 0);
    }

    public void addMission(String name, String description, Vessel vessel, Set<Kerbal> crew) {
        missions.add(new Mission(name, description, vessel, crew));
    }

    public void addVesselConcept(String name, VesselType type, VesselProperties... properties) {
        vessels.add(new VesselConcept(name, type, properties));
    }

    public void addVesselInstance(VesselConcept concept) {
        for (VesselConcept v : vessels) {
            if (v.getName().equals(concept.getName()) && v.getIteration() == concept.getIteration()) {
                instances.add(new VesselInstance(concept));
                break;
            }
        }
    }

    public void markAssigned(Set<Kerbal> kerbalSet) {
        for (Kerbal k : kerbalSet) k.setOnDuty(true);
    }
}
