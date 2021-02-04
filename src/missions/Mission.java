package missions;

import controller.ControllerInterface;
import controller.GUIController;
import kerbals.Kerbal;
import other.KSPObject;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.KSPDate;
import other.util.Location;
import vessels.Vessel;

import java.util.*;

public class Mission extends KSPObject implements KSPObjectListener {

    public static final int ENCODE_FIELD_AMOUNT = 7; // ALWAYS ACCOUNT FOR DESCRIPTION
    private static final String DELIMITER = ":m:";

    // Persistent fields
    private String name;
    private Set<Long> vessels;
    private final Map<String, CrewDetails> crew; // perhaps replace with a new object?
    private final KSPDate start;
    private final List<MissionEvent> events;
    private boolean active = true;

    // Dynamic fields
    private Set<Vessel> vesselObjs;
    private Set<Kerbal> crewObjs;

    /** Generates a mission from scratch.
     * @param name Mission name
     * @param crew A map corresponding to the crew and their roles
     * @param start Mission start date
     */
    public Mission(ControllerInterface controller, String name, Map<Kerbal, String> crew, KSPDate start) {
        super(controller);
        this.name = name;
        this.vessels = new HashSet<>();
        Map<String, CrewDetails> crew2 = new HashMap<>();
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

        Set<Long> ret1 = new HashSet<>();
        if (!fields.get(2).equals("(none)")) for (String s : fields.get(2).split(DELIMITER)) ret1.add(Long.parseLong(s));
        this.vessels = ret1;

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
        String s = fields.get(5);
        if (s.equals("(none)")) {
            result = new LinkedList<>();
        } else {
            List<MissionEvent> ret2 = new LinkedList<>();
            String[] events = s.split(DELIMITER);
            for (String event : events) ret2.add(MissionEvent.fromString(getController(), event));
            result = ret2;
        }
        this.events = result;
        this.active = Boolean.parseBoolean(fields.get(6));
        setDescription(fields.get(0));
    }

    // Logic methods
    public void kerbalRescued(Kerbal kerbal, KSPDate dateRescued) {
        this.crew.put(kerbal.getName(), new CrewDetails(getController(), kerbal.getName(), "Rescued subject", dateRescued));
        logEvent(new MissionEvent(getController(), getName(), null, "Rescued " + kerbal.getName())); // TODO replace null with kerbal location
    }

    /** Executed whenever a kerbal unfortunately goes KIA. This method assumes the cause of death to not be vessel crash.
     * @param kerbal The unfortunate victim
     * @param location Location where the kerbal went KIA
     * @param details Additional KIA details
     */
    public void kerbalKIA(Kerbal kerbal, Location location, String details) {
        kerbal.KIA(this, location, crew.get(kerbal.getName()).getExpGained(), details);
    }

    // missionEnd()

    public void logEvent(MissionEvent event) {
        events.add(event);
    }

    // addKerbals()

    // addVessels()

    // Getter/Setter methods
    public String getName() {
        return name;
    }

    public Set<Long> getVessels() {
        return new HashSet<>(vessels);
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

        StringJoiner sj1 = new StringJoiner(DELIMITER);
        for (long l : vessels) sj1.add(Long.toString(l));
        if (sj1.toString().equals("")) sj1.add("(none)");
        ret.add(sj1.toString());

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
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", name));
        fields.add(new Field("Mission start", start.toString(true, true)));
        for (Vessel v : vesselObjs) fields.add(new Field("Vessel", v.toString()));
        fields.add(new Field("In progress?", active ? "Yes" : "No"));
        for (Map.Entry<String, CrewDetails> e : crew.entrySet())
            fields.add(new Field(e.getValue().getPosition(), e.getKey() + " Kerman, boarded at " + e.getValue().getBoardTime().toString(false, true)));
        for (MissionEvent ev : events) fields.add(new Field("Milestone", ev.toString()));

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
            System.err.println("WARNING: Expect a crash, kerbal" + k.getName() + " deleted while on active mission " + name);
            crewObjs.remove(k);
            CrewDetails details = crew.get(k.getName());
            crew.remove(k.getName());
            crew.put("[REDACTED#" + k.hashCode() + "]", details);
        }

        // Vessel deleted
        if (event.getSource() instanceof Vessel vi) {
            vesselObjs.remove(vi);
            vessels.remove(vi.getId());
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
