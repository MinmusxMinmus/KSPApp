package controller;

import kerbals.Kerbal;
import kerbals.Role;
import missions.Mission;
import vessels.Vessel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GUIController {

    private final List<Kerbal> kerbals;
    private final List<Mission> missions;
    private final List<Vessel> vessels;

    public GUIController() {
        this.kerbals = new LinkedList<>();
        this.missions = new LinkedList<>();
        this.vessels = new LinkedList<>();

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

    public void addKerbal(String name, boolean isMale, Role role, int level) {
        kerbals.add(new Kerbal(name, isMale, role, level));
    }

    public void addKerbal(String name, boolean isMale, Role role) {
        addKerbal(name, isMale, role, 0);
    }

    public void addMission(String name, String description, Vessel vessel, Set<Kerbal> crew) {
        missions.add(new Mission(name, description, vessel, crew));
    }

    public void addVessel(String name) {
    }

    public void markAssigned(Set<Kerbal> kerbalSet) {
        for (Kerbal k : kerbalSet) k.setOnDuty(true);
    }
}
