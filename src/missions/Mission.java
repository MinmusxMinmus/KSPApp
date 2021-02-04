package missions;

import controller.ControllerInterface;
import controller.GUIController;
import kerbals.Condecoration;
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

    public static final int ENCODE_FIELD_AMOUNT = 8; // ALWAYS ACCOUNT FOR DESCRIPTION
    private static final String DELIMITER = ":m:";

    // Persistent fields
    private String name;
    private final KSPDate start;
    private Set<Long> vessels;
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
    public Mission(ControllerInterface controller, String name, Map<Kerbal, String> crew, KSPDate start) {
        super(controller);
        this.name = name;
        this.vessels = new HashSet<>();
        Map<String, CrewDetails> crew2 = new HashMap<>();
        crew.keySet().forEach(k -> crew2.put(k.getName(), new CrewDetails(controller, k.getName(), crew.get(k), start)));
        this.crew = crew2;
        this.start = start;
        this.events = new LinkedList<>();
        this.condecorations = new HashSet<>();
    }

    /** Generate a mission from a list of fields stored in persistence. =
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

        StringJoiner vesselJoiner = new StringJoiner(DELIMITER);
        for (long l : vessels) vesselJoiner.add(Long.toString(l));
        if (vesselJoiner.toString().equals("")) vesselJoiner.add("(none)");

        StringJoiner crewJoiner = new StringJoiner(DELIMITER);
        for (Map.Entry<String, CrewDetails> e : crew.entrySet())
            crewJoiner.add(e.getKey() + "<>" + CrewDetails.toString(e.getValue()));
        if (crewJoiner.toString().equals("")) crewJoiner.add("(none)");

        StringJoiner missionJoiner = new StringJoiner(DELIMITER);
        for (MissionEvent event : events) {
            Collection<String> det = event.toStorableCollection();
            StringJoiner subj = new StringJoiner(MissionEvent.DELIMITER);
            for (String s : det) subj.add(s);
            missionJoiner.add(subj.toString());
        }
        if (missionJoiner.toString().equals("")) missionJoiner.add("(none)");

        StringJoiner condecorationJoiner = new StringJoiner(DELIMITER);
        for (Condecoration c : condecorations) condecorationJoiner.add(Condecoration.toString(c));
        if (condecorationJoiner.toString().equals("")) condecorationJoiner.add("(none)");

        ret.add(name);
        ret.add(start.toStorableString());
        ret.add(Boolean.toString(active));
        ret.add(vesselJoiner.toString());
        ret.add(crewJoiner.toString());
        ret.add(missionJoiner.toString());
        ret.add(condecorationJoiner.toString());

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
        for (Condecoration c : condecorations) fields.add(new Field("Condecoration", c.toString()));

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
