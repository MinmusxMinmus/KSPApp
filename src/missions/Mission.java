package missions;

import controller.ControllerInterface;
import controller.GUIController;
import kerbals.Condecoration;
import kerbals.Job;
import kerbals.Kerbal;
import other.KSPObject;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.CelestialBody;
import other.util.Field;
import other.util.KSPDate;
import other.util.Location;
import vessels.Vessel;
import vessels.VesselStatus;

import java.util.*;
import java.util.stream.Collectors;

public class Mission extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 8; // ALWAYS ACCOUNT FOR DESCRIPTION
    private static final String DELIMITER = ":m:";

    // Persistent fields
    private String name;
    private final KSPDate start;
    private final Set<Long> vessels;
    private boolean active = true;
    private final Map<String, CrewDetails> crew; // perhaps replace with a new object?
    private final List<MissionEvent> events;
    private final Set<Condecoration> condecorations;

    // Dynamic fields
    private Set<Vessel> vesselObjs;
    private Set<Kerbal> crewObjs;

    /** Generates a mission from scratch.
     * @param name Mission name
     * @param crew A map corresponding to the crew and their roles
     * @param start Mission start date
     */
    public Mission(ControllerInterface controller, String name, Map<Kerbal, String> crew, Set<Long> vessels, KSPDate start) {
        super(controller);
        this.name = name;
        this.vessels = vessels;
        Map<String, CrewDetails> crew2 = new HashMap<>();
        crew.keySet().forEach(k -> crew2.put(k.getName(), new CrewDetails(controller, k.getName(), crew.get(k), start)));
        this.crew = crew2;
        this.start = start;
        this.events = new LinkedList<>();
        this.condecorations = new HashSet<>();
    }

    /** Generate a mission from a list of fields stored in persistence.
     * @param fields List of fields
     */
    public Mission(GUIController controller, LinkedList<String> fields) {
        super(controller);
        setDescription(fields.get(0));
        this.name = fields.get(1);
        this.start = KSPDate.fromString(controller, fields.get(2));
        this.active = Boolean.parseBoolean(fields.get(3));

        Set<Long> vessels = new HashSet<>();
        if (!fields.get(4).equals("(none)")) for (String s : fields.get(4).split(DELIMITER)) vessels.add(Long.parseLong(s));
        this.vessels = vessels;

        Map<String, CrewDetails> crew = new HashMap<>();
        if (!fields.get(5).equals("(none)")) {
            Set<String> entries = new HashSet<>(Arrays.asList(fields.get(5).split(DELIMITER)));
            for (String e : entries) {
                String[] pair = e.split("<>");
                if (pair.length != 2) continue;
                crew.put(pair[0], CrewDetails.fromString(getController(), pair[1]));
            }
        }
        this.crew = crew;

        List<MissionEvent> events = new LinkedList<>();
        if (!fields.get(6).equals("(none)")) for (String event : fields.get(6).split(DELIMITER))
            events.add(MissionEvent.fromString(controller, event));
        this.events = events;

        Set<Condecoration> condecorations = new HashSet<>();
        if (!fields.get(7).equals("(none)")) for (String c : fields.get(7).split(DELIMITER))
            condecorations.add(Condecoration.fromString(controller, c));
        this.condecorations = condecorations;
    }

    // Logic methods
    public void missionEnd(String comment, KSPDate date) {
        // Last event location, or KSC
        Location lastLocation;
        if (events.isEmpty()) lastLocation = new Location(false, CelestialBody.KERBIN);
        else lastLocation = events.get(events.size() - 1).getLocation();
        // Mission end event
        logEvent(lastLocation, date, "Mission end: " + comment);
        // Finish mission for all living kerbals involved
        for (Kerbal k : crewObjs.stream().filter(k -> !k.isKIA()).collect(Collectors.toSet())) k.missionEnd(this, comment);
        // Finish mission for all vessels involved
        for (Vessel v : vesselObjs) v.missionEnd(this);
        // End mission
        this.active = false;
    }

    public void logEvent(Location location, KSPDate date,  String details) {
        events.add(new MissionEvent(getController(), getName(), date, location, details));
    }

    public boolean addCrew(Kerbal k, String position, KSPDate date, String details) {
        // Check for crew already in mission
        if (crew.containsKey(k.getName()) || crewObjs.contains(k)) {
            CrewDetails detailss = crew.get(k.getName());
            if (detailss == null) return false; // Who knows, maybe only the object is there

            // If the position is the same, don't update
            if (detailss.getPosition().equals(position)) return false;
            // Update otherwise
            logEvent(k.getLocation(), date, k.getName() + " Kerman changed position from \"" + detailss.getPosition() + "\" to \"" + position + "\": " + details);

            crew.put(k.getName(), new CrewDetails(getController(), k.getName(), position, date));

            return true;
        }

        logEvent(k.getLocation(), date, k.getName() + " Kerman joined the mission as " + position + ": " + details);
        crewObjs.add(k);
        crew.put(k.getName(), new CrewDetails(getController(), k.getName(), position, date));
        k.addEventListener(this);
        k.missionStart(this);
        return true;
    }
    public boolean removeCrew(Kerbal k, KSPDate date, String details) {
        // Doesn't remove crew if crew is not in the mission
        if (!(crew.containsKey(k.getName()) || crewObjs.contains(k))) return false;
        crewObjs.remove(k);
        crew.remove(k.getName());
        k.removeEventListener(this);
        k.missionEnd(this, "Dismissed from the mission");
        logEvent(k.getLocation(), date, k.getName() + " Kerman (" + getCrewDetails(k).getPosition() + ") dismissed from the mission: " + details);
        return true;
    }
    public void updateCrew(Set<Kerbal> crewToRemove, Map<Kerbal, String> crewToAdd, KSPDate date, String details) {
        for (Kerbal k : crewToRemove) removeCrew(k, date, details);
        for (Map.Entry<Kerbal, String> e : crewToAdd.entrySet()) addCrew(e.getKey(), e.getValue(), date, details);
    }
    public void moveCrew(Kerbal k, Location location, KSPDate date, String details) {
        k.setLocation(location);
        logEvent(location, date, k.getName() + " Kerman changed location to " + location + ": " + details);
    }
    public void leftVessel(Kerbal k, Vessel v, KSPDate date, String details) {
        if (!crewObjs.contains(k)) System.err.println("WARNING: Editing kerbal not in mission! Mission: " + name + ", kerbal: " + k.getName());
        k.leaveVessel(v);
        logEvent(k.getLocation(), date, k.getName() + " Kerman left vessel " + v.getName() + " with ID " + v.getId() + ": " + details);
    }
    public void enteredVessel(Kerbal k, Vessel v, KSPDate date, String details) {
        if (!crewObjs.contains(k)) System.err.println("WARNING: Editing kerbal not in mission! Mission: " + name + ", kerbal: " + k.getName());
        k.enterVessel(v);
        logEvent(k.getLocation(), date, k.getName() + " Kerman boarded vessel " + v.getName() + " with ID " + v.getId() + ": " + details);
    }
    public void kerbalRescued(String name, boolean male, boolean badass, Job job, KSPDate date, Vessel rescuer, String position, String details) {
        // Create kerbal, log rescue event
        Kerbal rescuee = new Kerbal(getController(), name, male, badass, job, this.name, date, rescuer.getLocation(), rescuer);
        logEvent(rescuer.getLocation(), date, "Rescued " + name + " Kerman " + rescuer.getLocation() + ": " + details);
        // Add kerbal to database
        getController().addKerbal(rescuee);
        // Update kerbal mission
        rescuee.missionStart(this);
        // Add to crew
        addCrew(rescuee, position, date, details);
        // Add to vessel
        rescuer.addCrew(rescuee);
    }
    public void KIA(Kerbal k, KSPDate date, String details) {
        k.KIA();
        logEvent(k.getLocation(), date, k.getName() + " Kerman went KIA: " + details);
    }

    public boolean addVessel(Vessel v, KSPDate date, String details) {
        logEvent(v.getLocation(), date, "\"" + v.getName() + "\" vessel joined the mission: " + details);
        vesselObjs.add(v);
        vessels.add(v.getId());
        v.missionStart(this);
        return true;
    }
    public boolean removeVessel(Vessel v, KSPDate date, String details) {
        logEvent(v.getLocation(), date, "\"" + v.getName() + "\" vessel dismissed from the mission: " + details);
        vesselObjs.remove(v);
        vessels.remove(v.getId());
        v.missionEnd(this);
        return true;
    }
    public void updateVessels(List<Vessel> vesselsToRemove, List<Vessel> vesselsToAdd, KSPDate date, String details) {
        for (Vessel v : vesselsToRemove) removeVessel(v, date, details);
        for (Vessel v : vesselsToAdd) addVessel(v, date, details);
    }
    public void updateVessel(Vessel v, VesselStatus status, Location location, KSPDate date, String details) {
        v.setLocation(location);
        if (status.equals(VesselStatus.CRASHED)) {
            v.setCrashed(date, name, details);
            getController().vesselCrashed(v);
            logEvent(location, date, "Vessel " + v.getName() + " with  ID " + v.getId() + " crashed while " + location + ": " + details);
        } else {
            v.setStatus(status);
            logEvent(location, date, "Vessel " + v.getName() + " with  ID " + v.getId() + " marked as " + status.toString() + " while " + location + ": " + details);
        }
    }

    public void awardCondecoration(Kerbal k, String title, String condecoration, KSPDate date) {
        if (!crewObjs.contains(k)) System.err.println("WARNING: Awarding condecoration to non-mission member! Mission: " + name + ", kerbal name: " + k.getName());
        Condecoration c = new Condecoration(getController(), k.getName(), name, date, title, condecoration);
        condecorations.add(c);
        k.addCondecoration(c);
        logEvent(k.getLocation(), date, "Awarded condecoration to " + k.getName() + " Kerman");
    }

    // Getter/Setter methods
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public KSPDate getStart() {
        return start;
    }

    public Set<Vessel> getVessels() {
        return new HashSet<>(vesselObjs);
    }

    public Set<Vessel> getVessels(VesselStatus status) {
        return vesselObjs.stream()
                .filter(v -> v.getStatus().equals(status))
                .collect(Collectors.toSet());
    }

    public boolean isActive() {
        return active;
    }

    public Set<Kerbal> getCrew() {
        return Collections.unmodifiableSet(crewObjs);
    }

    public CrewDetails getCrewDetails(Kerbal kerbal) {
        return crew.get(kerbal.getName());
    }

    public List<MissionEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    // Overrides
    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = super.toStorableCollection();

        StringJoiner vesselJoiner = new StringJoiner(DELIMITER);
        for (long l : vessels) vesselJoiner.add(Long.toString(l));
        if (vesselJoiner.toString().equals("")) vesselJoiner.add("(none)");

        StringJoiner crewJoiner = new StringJoiner(DELIMITER);
        for (Map.Entry<String, CrewDetails> e : crew.entrySet())
            crewJoiner.add(e.getKey() + "<>" + CrewDetails.toString(e.getValue()));
        if (crewJoiner.toString().equals("")) crewJoiner.add("(none)");

        StringJoiner eventJoiner = new StringJoiner(DELIMITER);
        for (MissionEvent event : events) eventJoiner.add(MissionEvent.toString(event));
        if (eventJoiner.toString().equals("")) eventJoiner.add("(none)");

        StringJoiner condecorationJoiner = new StringJoiner(DELIMITER);
        for (Condecoration c : condecorations) condecorationJoiner.add(Condecoration.toString(c));
        if (condecorationJoiner.toString().equals("")) condecorationJoiner.add("(none)");

        ret.add(name);
        ret.add(start.toStorableString());
        ret.add(Boolean.toString(active));
        ret.add(vesselJoiner.toString());
        ret.add(crewJoiner.toString());
        ret.add(eventJoiner.toString());
        ret.add(condecorationJoiner.toString());

        return ret;
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", name));
        fields.add(new Field("Mission start", start.toString(true, true)));
        fields.add(new Field("In progress?", active ? "Yes" : "No"));
        for (Kerbal k : crewObjs) {
            CrewDetails cd = crew.get(k.getName());
            fields.add(new Field(cd.getPosition(), (k.isKIA() ? "(KIA) " : "") + k.getName() + " Kerman, boarded at " + cd.getBoardTime().toString(false, true)));
        }
        for (Vessel v : vesselObjs) fields.add(new Field("Vessel", v.toString()));
        for (Condecoration c : condecorations) fields.add(new Field("Condecoration", c.toString()));
        for (MissionEvent ev : events) {
            String date = "(" + ev.getDate().toString(false, true) + ")";
            String location = ev.getLocation().toString();
            fields.add(new Field("Event " + date + " " + location, ev.getDetails()));
        }

        return fields;
    }

    @Override
    public void ready() {
        // Get vessel
        this.vesselObjs = new HashSet<>();
        for (Vessel v : getController().getVessels()) if (vessels.contains(v.getId())) {
            vesselObjs.add(v);
            v.addEventListener(this);
        }
        for (Vessel v : getController().getCrashedVessels()) if (vessels.contains(v.getId())) {
            vesselObjs.add(v);
            v.addEventListener(this);
        }


        // Get crew
        this.crewObjs = new HashSet<>();
        for (Kerbal k : getController().getKerbals()) if (crew.containsKey(k.getName())) {
            crewObjs.add(k);
            k.addEventListener(this);
        }

        // Crew check
        if (crewObjs.size() != crew.keySet().size())
            System.err.println("WARNING: Crew member miscount in mission \"" + name + "\", this usually means that certain kerbals weren't found on the database.\n" +
                    "Expected " + crew.keySet().size() + ", got " + crewObjs.size());
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Kerbal deleted
        if (event.getSource() instanceof Kerbal k) {
            crewObjs.remove(k);
            crew.remove(k.getName());
        }

        // Vessel deleted
        if (event.getSource() instanceof Vessel v) {
            vesselObjs.remove(v);
            vessels.remove(v.getId());
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
