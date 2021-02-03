package controller;

import kerbals.Job;
import kerbals.Kerbal;
import missions.Mission;
import other.*;
import other.interfaces.ControllerInterface;
import other.util.Destination;
import other.util.KSPDate;
import persistencelib.Atom;
import persistencelib.Key;
import persistencelib.StorageManager;
import persistencelib.Version;
import vessels.*;

import java.io.IOException;
import java.util.*;

public class GUIController implements ControllerInterface {

    private static final String KERBAL_REGION = "Kerbals";
    private static final String MISSION_REGION = "Missions";
    private static final String VESSEL_REGION = "Vessels";
    private static final String CONCEPT_REGION = "Concepts";
    private static final String INSTANCE_REGION = "Instances";
    private static final String CRASHED_REGION = "Crashed";

    private final List<Kerbal> kerbals;
    private final List<Mission> missions;
    private final List<Concept> concepts;
    private final List<Vessel> instances;
    private final List<Vessel> crashedInstances = new LinkedList<>();

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
        getPersistenceConcepts();
        getPersistenceInstances();
        getPersistenceCrashedInstances();

        for (Kerbal k : kerbals) k.ready();
        for (Mission m : missions) m.ready();
        for (Concept v : concepts) v.ready();
        for (Vessel v : instances) v.ready();
        for (Vessel v : crashedInstances) v.ready();
    }

    private void getPersistenceCrashedInstances() {
        if (manager.getRegion(CRASHED_REGION) == null) manager.addRegion(CRASHED_REGION);
        Atom atom = manager.getRegion(CRASHED_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {

                    if (c.size() != Vessel.ENCODE_FIELD_AMOUNT) {
                        System.err.println("WARNING: Corrupt crashed vessel instance found: " + c + "\nExpected " +
                                Vessel.ENCODE_FIELD_AMOUNT + " fields, got " + c.size());
                        return;
                    }
                    crashedInstances.add(new Vessel(this, new LinkedList<>(c)));
                });
    }

    private void getPersistenceInstances() {
        if (manager.getRegion(INSTANCE_REGION) == null) manager.addRegion(INSTANCE_REGION);
        Atom atom = manager.getRegion(INSTANCE_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {
                    if (c.size() != Vessel.ENCODE_FIELD_AMOUNT) {
                        System.err.println("WARNING: Corrupt vessel instance found: " + c + "\nExpected " +
                                Vessel.ENCODE_FIELD_AMOUNT + " fields, got " + c.size());
                        return;
                    }
                    instances.add(new Vessel(this, new LinkedList<>(c)));
                });
    }

    private void getPersistenceConcepts() {
        if (manager.getRegion(CONCEPT_REGION) == null) manager.addRegion(CONCEPT_REGION);
        Atom atom = manager.getRegion(CONCEPT_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {

                    if (c.size() != Concept.ENCODE_FIELD_AMOUNT) {
                        System.err.println("WARNING: Corrupt vessel concept found: " + c + "\nExpected " +
                                Concept.ENCODE_FIELD_AMOUNT + " fields, got " + c.size());
                        return;
                    }
                    concepts.add(new Concept(this, new LinkedList<>(c)));
                });
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
                    Kerbal k = new Kerbal(this, new LinkedList<>(c));
                    kerbals.add(k);
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
                    missions.add(new Mission(this, new LinkedList<>(c)));
                });
    }


    public void createKerbalHired(String name, boolean isMale, boolean badass, Job job, KSPDate hiringDate) {
        Kerbal k = new Kerbal(this, name, isMale, badass, job, "Hired", hiringDate);
        addKerbal(k);
        k.ready();
    }

    public void createMission(String name, String description, Vessel vessel, Map<Kerbal, String> crew, KSPDate missionStart) {
        Mission m = new Mission(this, name, ((Vessel) vessel).getId(), crew, missionStart);
        for (Kerbal k : crew.keySet()) k.missionStart(m);
        m.setDescription(description);
        addMission(m);
        m.ready();
    }

    public void createMission(String name, String description, Concept vessel, Map<Kerbal, String> crew, KSPDate missionStart) {
        Mission m = new Mission(this, name, (Concept) vessel, crew, missionStart);
        for (Kerbal k : crew.keySet()) k.missionStart(m);
        m.setDescription(description);
        addMission(m);
        m.ready();
    }

    public void createConcept(String name, VesselType type, Concept redesign, KSPDate creationDate, Destination[] destinations, VesselProperty... properties) {
        Concept vc;
        // From scratch, type != null
        if (redesign == null) vc = new Concept(this, name, type, creationDate, destinations, properties);
        // Inspired, type == null;
        else vc = new Concept(this, name, redesign, creationDate, destinations, properties);
        addConcept(vc);
        vc.ready();
    }

    public void delete(KSPObject object, String status) {
        object.fireDeletionEvent(status);
        if (object instanceof Kerbal k ) kerbals.remove(k);
        else if (object instanceof Mission m ) missions.remove(m);
        else if (object instanceof Concept vc) concepts.remove(vc);
        else if (object instanceof Vessel vi) {
            instances.remove(vi); // One of these will work, you know
            crashedInstances.remove(vi);
        }
    }

    public long createInstance(Concept concept, int rng, Mission mission) {
        Vessel vi = new Vessel(this, concept, new Random(rng).nextLong(), mission);
        addInstance(vi);
        vi.ready();
        return vi.getId();
    }

    public boolean saveChanges() {
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

        if (manager.getRegion(CONCEPT_REGION) == null) manager.addRegion(CONCEPT_REGION);
        atom = manager.getRegion(CONCEPT_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Concept m : concepts) {
            atom.addItem(new Key(Integer.toString(count)), m.toStorableCollection());
            count++;
        }
        manager.replaceRegion(atom);

        if (manager.getRegion(INSTANCE_REGION) == null) manager.addRegion(INSTANCE_REGION);
        atom = manager.getRegion(INSTANCE_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Vessel m : instances) {
            atom.addItem(new Key(Integer.toString(count)), m.toStorableCollection());
            count++;
        }
        manager.replaceRegion(atom);

        if (manager.getRegion(CRASHED_REGION) == null) manager.addRegion(CRASHED_REGION);
        atom = manager.getRegion(CRASHED_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Vessel m : crashedInstances) {
            atom.addItem(new Key(Integer.toString(count)), m.toStorableCollection());
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
        // Delete everything
        kerbals.clear();
        missions.clear();
        concepts.clear();
        instances.clear();
        crashedInstances.clear();

        // Read from manager again
        getPersistenceKerbals();
        getPersistenceMissions();
        getPersistenceConcepts();
        getPersistenceInstances();
        getPersistenceCrashedInstances();
    }

    @Override
    public Kerbal getKerbal(String name) {
        return kerbals.stream().filter(k -> k.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public Mission getMission(String name) {
        return missions.stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public Concept getConcept(String name) {
        return concepts.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public Vessel getInstance(long id) {
        return instances.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    @Override
    public Vessel getCrashedInstance(long id) {
        return crashedInstances.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    @Override
    public Set<Kerbal> getKerbals() {
        return new HashSet<>(kerbals);
    }

    @Override
    public Set<Mission> getMissions() {
        return new HashSet<>(missions);
    }

    @Override
    public Set<Concept> getConcepts() {
        concepts.sort((vc1, vc2) -> {
            // Different family
            if (!vc1.getName().equals(vc2.getName())) return vc1.getName().compareTo(vc2.getName());
            // Same family, refer to iteration
            return vc2.getIteration() - vc1.getIteration();
        });
        return new HashSet<>(concepts);
    }

    @Override
    public Set<Vessel> getInstances() {
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
        return new HashSet<>(instances);
    }

    @Override
    public Set<Vessel> getCrashedInstances() {
        return new HashSet<>(crashedInstances);
    }

    @Override
    public void addKerbal(Kerbal kerbal) {
        kerbals.add(kerbal);
    }

    @Override
    public void addMission(Mission mission) {
        missions.add(mission);
    }

    @Override
    public void addConcept(Concept concept) {
        concepts.add(concept);
    }

    @Override
    public void addInstance(Vessel instance) {
        instances.add(instance);
    }

    @Override
    public void instanceRecovered(Vessel instance) {
        instance.fireDeletionEvent("Recovered lmao");
        instances.remove(instance);
    }

    @Override
    public void instanceCrashed(Vessel vessel) {
        instances.remove(vessel);
        crashedInstances.add(vessel);
    }
}
