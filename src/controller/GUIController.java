package controller;

import kerbals.Job;
import kerbals.Kerbal;
import missions.Mission;
import other.KSPDate;
import persistencelib.Atom;
import persistencelib.Key;
import persistencelib.StorageManager;
import persistencelib.Version;
import vessels.*;

import java.io.IOException;
import java.util.*;

public class GUIController {

    private static final String KERBAL_REGION = "Kerbals";
    private static final String MISSION_REGION = "Missions";
    private static final String VESSEL_REGION = "Vessels";

    private final List<Kerbal> kerbals;
    private final List<Mission> missions;
    private final List<VesselConcept> concepts;
    private final List<VesselInstance> instances;

    // Persistence

    private final StorageManager manager;

    public GUIController() throws IOException {
        this.kerbals = new LinkedList<>();
        this.missions = new LinkedList<>();
        this.concepts = new LinkedList<>();
        this.instances = new LinkedList<>();

        manager = new StorageManager("KSPDB", Version.V100);

        getPersistenceKerbals();
        getPersistenceMissions();
        getPersistenceVessels();
    }

    private void getPersistenceKerbals() {
        if (manager.getRegion(KERBAL_REGION) == null) manager.addRegion(KERBAL_REGION);
        Atom atom = manager.getRegion(KERBAL_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {
                    if (c.size() != Kerbal.ENCODE_FIELD_AMOUNT) {
                        System.err.println("WARNING: Corrupt kerbal found: " + c + "\nExpected " +
                                Kerbal.ENCODE_FIELD_AMOUNT + " fields, got " + c.size());
                        return;
                    }
                    kerbals.add(new Kerbal(new LinkedList<>(c)));
                });
    }

    private void getPersistenceMissions() {
        if (manager.getRegion(MISSION_REGION) == null) manager.addRegion(MISSION_REGION);
        Atom atom = manager.getRegion(MISSION_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {
                    if (c.size() != Mission.ENCODE_FIELD_AMOUNT) {
                        System.err.println("WARNING: Corrupt mission found: " + c + "\nExpected "
                                + Mission.ENCODE_FIELD_AMOUNT + " fields, got " + c.size());
                        return;
                    }
                    missions.add(new Mission(new LinkedList<>(c)));
                });
    }

    private void getPersistenceVessels() {
        if (manager.getRegion(VESSEL_REGION) == null) manager.addRegion(VESSEL_REGION);
        Atom atom = manager.getRegion(VESSEL_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {
                    // Special case: first entry contains vessel type
                    // Remove first entry
                    LinkedList<String> fields = new LinkedList<>(c);
                    fields.removeFirst();
                    // Check vessel type
                    switch (new LinkedList<>(c).get(0)) {
                        case Vessel.CONCEPT_STRING -> {
                            if (fields.size() != VesselConcept.ENCODE_FIELD_AMOUNT) {
                                System.err.println("WARNING: Corrupt vessel concept found: " + fields + "\nExpected " +
                                        VesselConcept.ENCODE_FIELD_AMOUNT + " fields, got " + fields.size());
                                return;
                            }
                            concepts.add(new VesselConcept(fields));
                        }
                        case Vessel.INSTANCE_STRING -> {
                            if (fields.size() != VesselInstance.ENCODE_FIELD_AMOUNT) {
                                System.err.println("WARNING: Corrupt vessel instance found: " + fields + "\nExpected " +
                                        VesselInstance.ENCODE_FIELD_AMOUNT + " fields, got " + fields.size());
                                return;
                            }
                            instances.add(new VesselInstance(fields));
                        }

                        default -> System.err.println("WARNING: Unknown vessel found: " + c + "Got " +
                                c.size() + " fields");
                    }
                });
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
        concepts.sort((vc1, vc2) -> {
            // Different family
            if (!vc1.getName().equals(vc2.getName())) return vc1.getName().compareTo(vc2.getName());
            // Same family, refer to iteration
            return vc2.getIteration() - vc1.getIteration();
        });
        return concepts.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    public List<VesselInstance> getVesselInstances() {
        instances.sort((vi1, vi2) -> {
            // Different family
            if (!vi1.getName().equals(vi2.getName())) return vi1.getName().compareTo(vi2.getName());
            // Same family, refer to iteration
            if (vi1.getIteration() != vi2.getIteration()) return vi2.getIteration() - vi1.getIteration();
            // Same iteration, refer to location
            if (!vi1.getLocation().equals(vi2.getLocation())) return vi1.getLocation().compareTo(vi2.getLocation());
            // Same location, refer to specific location
            if (vi1.isInSpace() != vi2.isInSpace()) return vi1.isInSpace() ? 1 : -1; // Vessels in space take priority
            // Same specific location, apply random order
            return vi2.hashCode() - vi1.hashCode();
        });
        return Collections.unmodifiableList(instances);
    }

    public VesselInstance getVesselInstance(String name) {
        return instances.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    public void addKerbalHired(String name, boolean isMale, Job job, KSPDate hiringDate) {
        addKerbal(new Kerbal(name, isMale, job, "Hired", hiringDate));
    }

    public void addKerbalRescued(String name, boolean isMale, Job job, Mission origin, KSPDate rescueDate) {
        Kerbal rescued = new Kerbal(name, isMale, job, origin.getName(), rescueDate);
        origin.kerbalRescued(rescued, rescueDate);
        addKerbal(rescued);
    }

    private void addKerbal(Kerbal kerbal) {
        kerbals.add(kerbal);
    }

    public void addMission(String name, String description, Vessel vessel, Map<Kerbal, String> crew, KSPDate missionStart) {
        long vesselId = (vessel instanceof VesselInstance vi) ? vi.getId() : new Random(name.hashCode()).nextLong();
        Mission m = new Mission(name, vessel.getName(), vesselId, crew, missionStart);
        m.setDescription(description);
        markAssigned(crew.keySet());

        missions.add(m);
    }

    public void addVesselConcept(String name, VesselType type, Destination[] destinations, VesselProperty... properties) {
        concepts.add(new VesselConcept(name, type, destinations, properties));
    }

    public void addVesselInstance(VesselConcept concept) {
        VesselInstance vi = new VesselInstance(concept, new Random(concept.hashCode()).nextLong());
        instances.add(vi);
    }

    public void markAssigned(Set<Kerbal> kerbalSet) {
        for (Kerbal k : kerbalSet) k.setAvailable(false);
    }

    public boolean saveChanges() { // TODO optimize
        int count = 0; // Keys are unused, simple number

        if (manager.getRegion(KERBAL_REGION) == null) manager.addRegion(KERBAL_REGION);
        Atom atom = manager.getRegion(KERBAL_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Kerbal k : kerbals) {
            atom.addItem(new Key(Integer.toString(count)), k.toStorableCollection());
            count++;
        }
        manager.replaceRegion(atom);

        if (manager.getRegion(MISSION_REGION) == null) manager.addRegion(MISSION_REGION);
        atom = manager.getRegion(MISSION_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Mission m : missions) {
            atom.addItem(new Key(Integer.toString(count)), m.toStorableCollection());
            count++;
        }
        manager.replaceRegion(atom);

        if (manager.getRegion(VESSEL_REGION) == null) manager.addRegion(VESSEL_REGION);
        atom = manager.getRegion(VESSEL_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Vessel v: getVesselConcepts()) {
            atom.addItem(new Key(Integer.toString(count)), v.toStorableCollection());
            count++;
        }
        for (Vessel v: getVesselInstances()) {
            atom.addItem(new Key(Integer.toString(count)), v.toStorableCollection());
            count++;
        }
        manager.replaceRegion(atom);

        try {
            manager.save();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void discard() {

    }
}
