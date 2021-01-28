package missions;

import controller.GUIController;
import kerbals.Kerbal;
import other.*;
import vessels.VesselConcept;
import vessels.VesselInstance;

import java.util.*;
import java.util.stream.Collectors;

public class Mission extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 8; // ALWAYS ACCOUNT FOR DESCRIPTION
    private static final String DELIMITER = ":m:";

    private final String name;
    private final String vessel;
    private long vesselId;
    private final Map<String, CrewDetails> crew;
    private final KSPDate missionStart;
    private final List<MissionEvent> eventLog;
    private boolean active = true;

    // Dynamic fields
    private VesselInstance vesselInstance;
    private Set<Kerbal> crewObjects;

    /** Generates a mission from scratch, with a brand new vessel.
     * @param name Mission name
     * @param concept The vessel concept
     * @param crew A map corresponding to the crew and their roles
     * @param missionStart Mission start date
     */
    public Mission(ControllerInterface controller, String name, VesselConcept concept, Map<Kerbal, String> crew, KSPDate missionStart) {
        super(controller);
        this.name = name;
        this.vessel = concept.getName();
        this.vesselId = new Random(this.hashCode()).nextLong();
        // Creating the vessel
        Kerbal[] crew2 = new Kerbal[crew.keySet().size()];
        crew.keySet().toArray(crew2);
        controller.addInstance(new VesselInstance(controller, concept, vesselId, crew2));
        // Formatting crew map
        TreeMap<String, CrewDetails> crew3 = new TreeMap<>();
        crew.keySet().forEach(k -> crew3.put(k.getName(), new CrewDetails(controller, k.getName(), crew.get(k), missionStart)));
        this.crew = crew3;
        this.missionStart = missionStart;
        eventLog = new LinkedList<>();
    }

    /** Generates a mission from scratch, launched from an existing vessel.
     * @param name Mission name
     * @param vesselId ID of the specific vessel
     * @param crew A map corresponding to the crew and their roles
     * @param missionStart Mission start date
     */
    public Mission(ControllerInterface controller, String name, long vesselId, Map<Kerbal, String> crew, KSPDate missionStart) {
        super(controller);
        this.name = name;
        this.vessel = controller.getInstance(vesselId).getConcept();
        this.vesselId = vesselId;
        TreeMap<String, CrewDetails> crew2 = new TreeMap<>();
        crew.keySet().forEach(k -> crew2.put(k.getName(), new CrewDetails(controller, k.getName(), crew.get(k), missionStart)));
        this.crew = crew2;
        this.missionStart = missionStart;
        eventLog = new LinkedList<>();
    }

    /** Generate a mission from a list of fields stored in persistence. =
     * @param fields List of fields
     */
    public Mission(GUIController controller, LinkedList<String> fields) {
        super(controller);
        this.name = fields.get(1);
        this.vessel = fields.get(2);
        this.vesselId = Long.parseLong(fields.get(3));
        this.crew = crewFromString(fields.get(4));
        this.missionStart = KSPDate.fromString(controller, fields.get(5));
        this.eventLog = eventLogFromString(fields.get(6));
        this.active = Boolean.parseBoolean(fields.get(7));
        setDescription(fields.get(0));
    }

    private String crewToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (Map.Entry<String, CrewDetails> e : crew.entrySet()) {
            joiner.add(e.getKey() + "<>" + CrewDetails.toString(e.getValue()));
        }
        return joiner.toString();
    }

    private Map<String, CrewDetails> crewFromString(String s) {
        Map<String, CrewDetails> ret = new HashMap<>();
        Set<String> entries = new HashSet<>(Arrays.asList(s.split(DELIMITER)));
        for (String e : entries) {
            String[] pair = e.split("<>");
            if (pair.length != 2) continue;
            ret.put(pair[0], CrewDetails.fromString(getController(), pair[1]));
        }
        return ret;
    }

    private List<MissionEvent> eventLogFromString(String s) {
        if (s.equals("(none)")) return new LinkedList<>();
       List<MissionEvent> ret = new LinkedList<>();
       String[] events = s.split(DELIMITER);
       for (String event : events) ret.add(MissionEvent.fromString(getController(), event));
       return ret;
    }

    private String eventLogToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (MissionEvent event : eventLog) {
            Collection<String> det = event.toStorableCollection();
            StringJoiner subj = new StringJoiner(MissionEvent.DELIMITER);
            for (String s : det) subj.add(s);
            joiner.add(subj.toString());
        }
        return joiner.toString().equals("") ? "(none)" : joiner.toString();
    }


    public void kerbalRescued(Kerbal kerbal, KSPDate dateRescued) {
        this.crew.put(kerbal.getName(), new CrewDetails(getController(), kerbal.getName(), "Rescued subject", dateRescued));
        logEvent(new MissionEvent(getController(), getName(), vesselInstance.isInSpace(), vesselInstance.getLocation(), "Rescued " + kerbal.getName()));
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
        String deathDetails = "Died during destruction of " + vesselInstance.getName();
        // Vessel
        vesselInstance.destroyed(details);
        // Dynamic vessel is not set to null, because the vessel is not removed from memory.3
    }

    /**
     * Executed when a mission ends via recovery of every single member and vessel.
     * @param status Mission end summary
     */
    public void recoverEnd(String status) {
        this.active = false;

        // All crew in vessel check
        if (crew.size() != vesselInstance.getCrew().size())
            System.err.println("WARNING: Not all crew is in the current vessel. All crew: " + crew.size() + ", vessel crew: " + vesselInstance.getCrew().size());

        // Recover vessel
        if (vesselInstance != null) vesselInstance.recover();
        vesselInstance = null;

        // Log nominal end
        logEvent(new MissionEvent(getController(), name, vesselInstance.isInSpace(), vesselInstance.getLocation(), "Nominal end: " + status));
    }

    /** Executed when a mission ends via total destruction of all crew members and vessel involved.
     * @param status Mission end description
     */
    public void catastrophicEnd(String status) {
        this.active = false;

        // All crew members + vessel should be gone
        if (!crewObjects.isEmpty()) System.err.println("WARNING: Total destruction mission end with kerbal objects still around. Mission: " + name + ", crew count: " + crewObjects.size());
        if (!crew.isEmpty()) System.err.println("WARNING: Total destruction mission end with kerbal names still around. Mission: " + name + ", crew count: " + crew.keySet().size());
        if (vesselInstance != null) System.err.println("WARNING: Total destruction mission end with vessel instance still around. Mission: " + name + ", instance: " + vesselInstance.getName());
        if (vessel != null) System.err.println("WARNING: Total destruction mission end with vessel name still around. Mission: " + name + ", vessel name " + vessel);
        // TODO perhaps replace warnings with a return false? This shouldn't happen anyway, it's for debugging.

        // Log catastrophic end
        logEvent(new MissionEvent(getController(), name, vesselInstance.isInSpace(), vesselInstance.getLocation(), "Catastrophic end: " + status));
    }

    public void logEvent(MissionEvent event) {
        eventLog.add(event);
    }



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

    public List<MissionEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    public KSPDate getMissionStart() {
        return missionStart;
    }


    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = super.toStorableCollection();

        ret.add(name);
        ret.add(vessel);
        ret.add(Long.toString(vesselId));
        ret.add(crewToString());
        ret.add(missionStart.toStorableString());
        ret.add(eventLogToString());
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
        VesselInstance vessel = getController().getInstance(vesselId);

        // TODO format fields for completed missions

        fields.add(new Field("Name", name));
        fields.add(new Field("Mission start", missionStart.getTextRepresentation(true)));
        fields.add(new Field("Vessel", vesselInstance.getTextRepresentation()));
        fields.add(new Field("In progress?", active ? "Yes" : "No"));
        for (Map.Entry<String, CrewDetails> e : crew.entrySet())
            fields.add(new Field(e.getKey(), e.getValue().getTextRepresentation()));
        for (MissionEvent ev : eventLog) fields.add(new Field("Milestone", ev.getTextRepresentation()));

        return fields;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Kerbal deleted (???)
        if (event.getSource() instanceof Kerbal k) {
            crewObjects.remove(k);
            CrewDetails details = crew.get(k.getName());
            crew.remove(k.getName());
            crew.put("[REDACTED#" + k.hashCode() + "]", details);
        }

        // Vessel deleted (???)
        if (event.getSource() instanceof VesselInstance vi) {
            System.err.println("WARNING: Vessel " + vi.getName() + "#" + vi.getId() + " deleted from mission " + name + " unexpectedly. A crash will most likely happen soon!");
        }
    }

    @Override
    public void ready() {
        // Get vessel
        vesselInstance = getController().getInstance(vesselId);
        if (vesselInstance != null) vesselInstance.addEventListener(this);

        // Get crew
        this.crewObjects = getController().getKerbals().stream()
                .filter(c -> crew.containsKey(c.getName()))
                .collect(Collectors.toSet());
        for (Kerbal k : crewObjects) k.addEventListener(this);

        // Crew check
        if (crewObjects.size() != crew.keySet().size())
            System.err.println("WARNING: Crew member miscount in mission \"" + name + "\". " +
                    "Expected " + crew.keySet().size() + ", got " + crewObjects.size());
    }
}
