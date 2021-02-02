package missions;

import controller.GUIController;
import kerbals.Kerbal;
import other.*;
import other.interfaces.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.CelestialBody;
import other.util.Field;
import other.util.KSPDate;
import vessels.VesselConcept;
import vessels.VesselInstance;

import java.util.*;
import java.util.stream.Collectors;

public class Mission extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 7; // ALWAYS ACCOUNT FOR DESCRIPTION
    private static final String DELIMITER = ":m:";

    // Persistent fields
    private String name;
    private long vesselId;
    private final Map<String, CrewDetails> crew;
    private final KSPDate start;
    private final List<MissionEvent> events;
    private boolean active = true;

    // Dynamic fields
    private VesselInstance vesselObj;
    private Set<Kerbal> crewObjs;

    // Constructors
    /** Generates a mission from scratch, with a brand new vessel.
     * @param name Mission name
     * @param concept The vessel concept
     * @param crew A map corresponding to the crew and their roles
     * @param start Mission start date
     */
    public Mission(ControllerInterface controller, String name, VesselConcept concept, Map<Kerbal, String> crew, KSPDate start) {
        super(controller);
        this.name = name;
        this.vesselId = new Random(this.hashCode()).nextLong();
        // Creating the vessel
        Kerbal[] crew2 = new Kerbal[crew.keySet().size()];
        crew.keySet().toArray(crew2);
        controller.addInstance(new VesselInstance(controller, concept, vesselId, this, crew2)); // TODO this might break, since VesselInstance accesses mission.getName()
        // Formatting crew map
        TreeMap<String, CrewDetails> crew3 = new TreeMap<>();
        crew.keySet().forEach(k -> crew3.put(k.getName(), new CrewDetails(controller, k.getName(), crew.get(k), start)));
        this.crew = crew3;
        this.start = start;
        events = new LinkedList<>();
    }

    /** Generates a mission from scratch, launched from an existing vessel.
     * @param name Mission name
     * @param vesselId ID of the specific vessel
     * @param crew A map corresponding to the crew and their roles
     * @param start Mission start date
     */
    public Mission(ControllerInterface controller, String name, long vesselId, Map<Kerbal, String> crew, KSPDate start) {
        super(controller);
        this.name = name;
        this.vesselId = vesselId;
        TreeMap<String, CrewDetails> crew2 = new TreeMap<>();
        crew.keySet().forEach(k -> crew2.put(k.getName(), new CrewDetails(controller, k.getName(), crew.get(k), start)));
        this.crew = crew2;
        this.start = start;
        events = new LinkedList<>();
    }

    /** Generate a mission from a list of fields stored in persistence. =
     * @param fields List of fields
     */
    public Mission(GUIController controller, LinkedList<String> fields) {
        super(controller);
        this.name = fields.get(1);
        this.vesselId = Long.parseLong(fields.get(2));

        Map<String, CrewDetails> ret = new HashMap<>();
        Set<String> entries = new HashSet<>(Arrays.asList(fields.get(3).split(DELIMITER)));
        for (String e : entries) {
            String[] pair = e.split("<>");
            if (pair.length != 2) continue;
            ret.put(pair[0], CrewDetails.fromString(getController(), pair[1]));
        }
        this.crew = ret;
        this.start = KSPDate.fromString(controller, fields.get(4));

        List<MissionEvent> result;
        String s = fields.get(6);
        if (s.equals("(none)")) {
            result = new LinkedList<>();
        } else {
            List<MissionEvent> ret1 = new LinkedList<>();
            String[] events = s.split(DELIMITER);
            for (String event : events) ret1.add(MissionEvent.fromString(getController(), event));
            result = ret1;
        }
        this.events = result;
        this.active = Boolean.parseBoolean(fields.get(7));
        setDescription(fields.get(0));
    }

    // Logic methods
    public void kerbalRescued(Kerbal kerbal, KSPDate dateRescued) {
        this.crew.put(kerbal.getName(), new CrewDetails(getController(), kerbal.getName(), "Rescued subject", dateRescued));
        logEvent(new MissionEvent(getController(), getName(), vesselObj.isInSpace(), vesselObj.getLocation(), "Rescued " + kerbal.getName()));
    }

    /** Executed whenever a kerbal unfortunately goes KIA. This method assumes the cause of death to not be vessel crash.
     * @param kerbal The unfortunate victim
     * @param inSpace True if the kerbal died in space, false otherwise
     * @param location Celestial body's SoI where the kerbal went KIA
     * @param details Additional KIA details
     */
    public void kerbalKIA(Kerbal kerbal, boolean inSpace, CelestialBody location, String details) {
        kerbal.KIA(this, inSpace, location, crew.get(kerbal.getName()).getExpGained(), details);
    }

    /** Executed when the current active vessel is completely destroyed. Also kills all kerbals involved.
     * @param details Destruction details
     */
    public void vesselDestroyed(String details) {
        // Victims
        String deathDetails = "Died during destruction of " + vesselObj.getName();
        // Vessel
        vesselObj.crash(details);
        // Dynamic vessel is not set to null, because the vessel is not removed from memory.3
    }

    /**
     * Executed when a mission ends via recovery of every single member and vessel.
     * @param status Mission end summary
     */
    public void recoverEnd(String status) {
        this.active = false;

        // All crew in vessel check
        if (crew.size() != vesselObj.getCrew().size())
            System.err.println("WARNING: Not all crew is in the current vessel. All crew: " + crew.size() + ", vessel crew: " + vesselObj.getCrew().size());

        // Recover vessel
        if (vesselObj != null) vesselObj.recover();
        vesselObj = null;

        // Log nominal end
        logEvent(new MissionEvent(getController(), name, vesselObj.isInSpace(), vesselObj.getLocation(), "Nominal end: " + status));
    }

    /** Executed when a mission ends via total destruction of all crew members and vessel involved.
     * @param status Mission end description
     */
    public void catastrophicEnd(String status) {
        this.active = false;

        // All crew members + vessel should be gone
        if (!crewObjs.isEmpty()) System.err.println("WARNING: Total destruction mission end with kerbal objects still around. Mission: " + name + ", crew count: " + crewObjs.size());
        if (!crew.isEmpty()) System.err.println("WARNING: Total destruction mission end with kerbal names still around. Mission: " + name + ", crew count: " + crew.keySet().size());
        if (vesselObj != null) System.err.println("WARNING: Total destruction mission end with vessel instance still around. Mission: " + name + ", instance: " + vesselObj.getName());
        // TODO perhaps replace warnings with a return false? This shouldn't happen anyway, it's for debugging.

        // Log catastrophic end
        logEvent(new MissionEvent(getController(), name, vesselObj.isInSpace(), vesselObj.getLocation(), "Catastrophic end: " + status));
    }

    public void logEvent(MissionEvent event) {
        events.add(event);
    }

    // Getter/Setter methods
    public String getName() {
        return name;
    }

    public long getVesselId() {
        return vesselId;
    }

    public Set<String> getCrew() {
        return Collections.unmodifiableSet(crew.keySet());
    }

    public CrewDetails getCrewDetails(Kerbal kerbal) {
        return crew.get(kerbal.getName());
    }

    public float getExperienceGained(Kerbal kerbal) {
        return crew.get(kerbal.getName()).getExpGained();
    }

    public List<MissionEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public KSPDate getStart() {
        return start;
    }

    // Overrides
    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = super.toStorableCollection();

        ret.add(name);

        ret.add(Long.toString(vesselId));
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (Map.Entry<String, CrewDetails> e : crew.entrySet()) {
            joiner.add(e.getKey() + "<>" + CrewDetails.toString(e.getValue()));
        }
        ret.add(joiner.toString());
        ret.add(start.toStorableString());

        StringJoiner joiner1 = new StringJoiner(DELIMITER);
        for (MissionEvent event : events) {
            Collection<String> det = event.toStorableCollection();
            StringJoiner subj = new StringJoiner(MissionEvent.DELIMITER);
            for (String s : det) subj.add(s);
            joiner1.add(subj.toString());
        }
        ret.add(joiner1.toString().equals("") ? "(none)" : joiner1.toString());
        ret.add(Boolean.toString(active));

        return ret;
    }

    @Override
    public String getTextRepresentation() {
        return name;
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", name));
        fields.add(new Field("Mission start", start.getTextRepresentation(true)));
        fields.add(new Field("Vessel", vesselId == 0 ? "[REDACTED]" : vesselObj == null ? "???" : vesselObj.getName()));
        fields.add(new Field("In progress?", active ? "Yes" : "No"));
        for (Map.Entry<String, CrewDetails> e : crew.entrySet())
            fields.add(new Field(e.getValue().getPosition(), e.getKey() + " Kerman, boarded at " + e.getValue().getBoardTime().getTextRepresentation(false, false)));
        for (MissionEvent ev : events) fields.add(new Field("Milestone", ev.getTextRepresentation()));

        return fields;
    }

    @Override
    public void ready() {
        // Get vessel
        if (vesselObj != null) vesselObj.addEventListener(this);

        // Get crew
        this.crewObjs = getController().getKerbals().stream()
                .filter(c -> crew.containsKey(c.getName()))
                .collect(Collectors.toSet());
        for (Kerbal k : crewObjs) k.addEventListener(this);

        // Crew check
        if (crewObjs.size() != crew.keySet().size())
            System.err.println("WARNING: Crew member miscount in mission \"" + name + "\", this usually means that certain kerbals weren't found on the database.\n" +
                    "Expected " + crew.keySet().size() + ", got " + crewObjs.size());
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Kerbal deleted
        if (event.getSource() instanceof Kerbal k) {
            System.err.println("WARNING: Expect a crash, kerbal" + k.getName() + " deleted while on active mission " + name);
            crewObjs.remove(k);
            CrewDetails details = crew.get(k.getName());
            crew.remove(k.getName());
            crew.put("[REDACTED#" + k.hashCode() + "]", details);
        }

        // Vessel deleted
        if (event.getSource() instanceof VesselInstance vi) {
            System.err.println("WARNING: Vessel " + vi.getName() + "#" + vi.getId() + " deleted from mission " + name + " unexpectedly. A crash will most likely happen soon!");
            vesselObj = null;
            vesselId = 0;
        }
    }
}
