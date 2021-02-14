package controller;

import kerbals.Job;
import kerbals.Kerbal;
import missions.Mission;
import other.KSPObject;
import other.util.CelestialBody;
import other.util.Destination;
import other.util.KSPDate;
import other.util.Location;
import persistencelib.Atom;
import persistencelib.Key;
import persistencelib.StorageManager;
import persistencelib.Version;
import vessels.Concept;
import vessels.Vessel;
import vessels.VesselProperty;
import vessels.VesselType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class GUIController implements ControllerInterface {

    private static final String KERBAL_REGION = "Kerbals";
    private static final String MISSION_REGION = "Missions";
    private static final String ARCHIVE_REGION = "Archives";
    private static final String CONCEPT_REGION = "Concepts";
    private static final String VESSEL_REGION = "Vessels";
    private static final String CRASHED_REGION = "Crashed";

    private final List<Kerbal> kerbals;
    private final List<Mission> missions;
    private final List<Mission> archives;
    private final List<Concept> concepts;
    private final List<Vessel> vessels;
    private final List<Vessel> crashedVessels;
    private final Random random;

    // Persistence

    private final StorageManager manager;

    public GUIController(String filename) throws IOException {
        this.random = new Random(LocalDate.now().hashCode());
        this.kerbals = new LinkedList<>();
        this.missions = new LinkedList<>();
        this.archives = new LinkedList<>();
        this.concepts = new LinkedList<>();
        this.vessels = new LinkedList<>();
        this.crashedVessels = new LinkedList<>();

        manager = new StorageManager(filename, Version.V100);

        getPersistenceKerbals();
        getPersistenceMissions();
        getPersistenceArchives();
        getPersistenceConcepts();
        getPersistenceVessels();
        getPersistenceCrashedVessels();

        ready();
    }

    private void getPersistenceArchives() {
        if (manager.getRegion(ARCHIVE_REGION) == null) manager.addRegion(ARCHIVE_REGION);
        Atom atom = manager.getRegion(ARCHIVE_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {
                    if (c.size() != Mission.ENCODE_FIELD_AMOUNT) {
                        System.err.println("WARNING: Corrupt archive found: " + c + "\nExpected "
                                + Mission.ENCODE_FIELD_AMOUNT + " fields, got " + c.size());
                        return;
                    }
                    archives.add(new Mission(this, new LinkedList<>(c)));
                });
    }

    private void getPersistenceCrashedVessels() {
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
                    crashedVessels.add(new Vessel(this, new LinkedList<>(c)));
                });
    }

    private void getPersistenceVessels() {
        if (manager.getRegion(VESSEL_REGION) == null) manager.addRegion(VESSEL_REGION);
        Atom atom = manager.getRegion(VESSEL_REGION);
        atom.getItems().stream()
                .map(atom::getItem)
                .forEach(c -> {
                    if (c.size() != Vessel.ENCODE_FIELD_AMOUNT) {
                        System.err.println("WARNING: Corrupt vessel instance found: " + c + "\nExpected " +
                                Vessel.ENCODE_FIELD_AMOUNT + " fields, got " + c.size());
                        return;
                    }
                    vessels.add(new Vessel(this, new LinkedList<>(c)));
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


    public void createKerbalHired(String name, boolean isMale, boolean badass, Job job, KSPDate hiringDate, String hiringReason, String description) {
        Kerbal k = new Kerbal(this, name, isMale, badass, job, hiringReason, hiringDate, new Location(false, CelestialBody.KERBIN), null); // Astronaut Complex
        k.setDescription(description);
        addKerbal(k);
    }

    public void createMission(String name, String description, Map<Kerbal, String> crew, List<Vessel> vessels, KSPDate missionStart) {
        Mission m = new Mission(this, name, crew, vessels.stream().map(Vessel::getId).collect(Collectors.toList()), missionStart);
        for (Kerbal k : crew.keySet()) k.missionStart(m);
        for (Vessel v : vessels) v.missionStart(m);
        m.setDescription(description);
        addMission(m);
    }

    public void createConcept(String name, VesselType type, Concept redesign, KSPDate creationDate, Destination[] destinations, VesselProperty... properties) {
        Concept vc;
        // From scratch, type != null
        if (redesign == null) vc = new Concept(this, name, type, creationDate, destinations, properties);
        // Inspired, type == null;
        else vc = new Concept(this, name, redesign, creationDate, destinations, properties);
        addConcept(vc);
    }

    public void delete(KSPObject object, String status) {
        object.fireDeletionEvent(status);
        if (object instanceof Kerbal k ) kerbals.remove(k);
        else if (object instanceof Mission m ) missions.remove(m);
        else if (object instanceof Concept vc) concepts.remove(vc);
        else if (object instanceof Vessel vi) {
            vessels.remove(vi); // One of these will work, you know
            crashedVessels.remove(vi);
        }
    }

    public long createVessel(Concept concept, Location location, KSPDate creationDate, Set<Kerbal> crew, Vessel... vessels) {
        Vessel vi = new Vessel(this, concept, creationDate, location, new HashSet<>(Arrays.asList(vessels)));
        addVessel(vi);
        for (Kerbal k : crew) {
            vi.addCrew(k);
            k.enterVessel(vi);
        }
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

        if (manager.getRegion(VESSEL_REGION) == null) manager.addRegion(VESSEL_REGION);
        atom = manager.getRegion(VESSEL_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Vessel m : vessels) {
            atom.addItem(new Key(Integer.toString(count)), m.toStorableCollection());
            count++;
        }
        manager.replaceRegion(atom);

        if (manager.getRegion(CRASHED_REGION) == null) manager.addRegion(CRASHED_REGION);
        atom = manager.getRegion(CRASHED_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Vessel m : crashedVessels) {
            atom.addItem(new Key(Integer.toString(count)), m.toStorableCollection());
            count++;
        }
        manager.replaceRegion(atom);

        if (manager.getRegion(ARCHIVE_REGION) == null) manager.addRegion(ARCHIVE_REGION);
        atom = manager.getRegion(ARCHIVE_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Mission m : archives) {
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

    public boolean saveMissionChanges() {
        int count = 0;
        if (manager.getRegion(MISSION_REGION) == null) manager.addRegion(MISSION_REGION);
        Atom atom = manager.getRegion(MISSION_REGION);
        // Remove all previous items
        for (Key key : atom.getItems()) atom.removeItem(key);
        // Add everything again
        for (Mission m : missions) {
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
        vessels.clear();
        crashedVessels.clear();

        // Read from manager again
        getPersistenceKerbals();
        getPersistenceMissions();
        getPersistenceConcepts();
        getPersistenceVessels();
        getPersistenceCrashedVessels();
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
        return vessels.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    @Override
    public Vessel getCrashedInstance(long id) {
        return crashedVessels.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
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
    public Mission getArchive(String name) {
        return archives.stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public Set<Mission> getArchives() {
        return new HashSet<>(archives);
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
    public Set<Vessel> getVessels() {
        vessels.sort((vi1, vi2) -> {
            // Different family
            if (!vi1.getName().equals(vi2.getName())) return vi1.getName().compareTo(vi2.getName());
            // Same family, refer to iteration
            if (vi1.getIteration() != vi2.getIteration()) return vi2.getIteration() - vi1.getIteration();
            // Same iteration, refer to location
            if (!vi1.getLocation().getCelestialBody().equals(vi2.getLocation().getCelestialBody())) return vi1.getLocation().getCelestialBody().compareTo(vi2.getLocation().getCelestialBody());
            // Same location, refer to specific location
            if (vi1.getLocation().isInSpace() != vi2.getLocation().isInSpace()) return vi1.getLocation().isInSpace() ? 1 : -1; // Vessels in space take priority
            // Same specific location, apply random order
            return vi2.hashCode() - vi1.hashCode();
        });
        return new HashSet<>(vessels);
    }

    @Override
    public Set<Vessel> getCrashedVessels() {
        return new HashSet<>(crashedVessels);
    }

    @Override
    public void addKerbal(Kerbal kerbal) {
        kerbal.ready();
        kerbals.add(kerbal);
    }

    @Override
    public void addMission(Mission mission) {
        mission.ready();
        missions.add(mission);
    }

    @Override
    public void addConcept(Concept concept) {
        concept.ready();
        concepts.add(concept);
    }

    @Override
    public long addVessel(Vessel instance) {
        instance.ready();
        vessels.add(instance);
        return instance.getId();
    }

    @Override
    public void missionFinished(Mission mission) {
        missions.remove(mission);
        archives.add(mission);
    }

    @Override
    public void vesselRecovered(Vessel vessel) {
        vessel.fireDeletionEvent("Recovered lmao");
        vessels.remove(vessel);
    }

    @Override
    public void vesselCrashed(Vessel vessel) {
        vessels.remove(vessel);
        crashedVessels.add(vessel);
    }

    @Override
    public void ready() {
        for (Kerbal k : kerbals) k.ready();
        for (Mission m : missions) m.ready();
        for (Concept v : concepts) v.ready();
        for (Vessel v : vessels) v.ready();
        for (Vessel v : crashedVessels) v.ready();
    }

    @Override
    public long rng() {
        return random.nextLong();
    }
}
