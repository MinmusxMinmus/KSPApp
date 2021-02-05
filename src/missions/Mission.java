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
    public void missionEnd(String comment) {
        // Finish mission for all kerbals involved
        for (Kerbal k : crewObjs) k.missionEnd(this, comment);
        // Finish mission for all vessels involved
        for (Vessel v : vesselObjs) v.missionEnd(this);
        // End mission
        this.active = false;
        setDescription(getDescription() + "\n\nMISSION END\n" + comment);
    }

    public void logEvent(Location location, KSPDate date,  String details) {
        events.add(new MissionEvent(getController(), getName(), date, location, details));
    }

    public boolean addCrew(Kerbal k, String position, KSPDate date) {
        logEvent(k.getLocation(), date, k.getName() + " Kerman joined the mission as " + position);
        crewObjs.add(k);
        crew.put(k.getName(), new CrewDetails(getController(), k.getName(), position, date));
        k.addEventListener(this);
        return true;
    }
    public boolean removeCrew(Kerbal k, KSPDate date) {
        logEvent(k.getLocation(), date, k.getName() + " Kerman (" + getCrewDetails(k).getPosition() + ") dismissed from the mission");
        crewObjs.remove(k);
        crew.remove(k.getName());
        k.removeEventListener(this);
        return true;
    }

    public boolean addVessel(Vessel v, KSPDate date) {
        logEvent(v.getLocation(), date, "\"" + v.getName() + "\" vessel joined the mission");
        vesselObjs.add(v);
        vessels.add(v.getId());
        v.addEventListener(this);
        return true;
    }
    public boolean removeVessel(Vessel v, KSPDate date) {
        logEvent(v.getLocation(), date, "\"" + v.getName() + "\" vessel dismissed from the mission");
        vesselObjs.remove(v);
        vessels.remove(v.getId());
        v.removeEventListener(this);
        return true;
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

    public Set<Long> getVessels() {
        return new HashSet<>(vessels);
    }

    public Set<String> getCrew() {
        return Collections.unmodifiableSet(crew.keySet());
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
            crewObjs.remove(k);
            crew.remove(k.getName());
            k.removeEventListener(this);
        }

        // Vessel deleted
        if (event.getSource() instanceof Vessel v) {
            vesselObjs.remove(v);
            vessels.remove(v.getId());
            v.removeEventListener(this);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
