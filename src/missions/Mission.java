package missions;

import kerbals.Kerbal;
import other.KSPDate;
import other.KSPObject;

import java.util.*;
import java.util.stream.Collectors;

class CrewDetails extends KSPObject {

    public static final String DELIMITER = ":cd:";
    public static final int ENCODE_FIELD_AMOUNT = 4;

    private final String subject;
    private final String position;
    private final KSPDate boardTime;
    private float expGained;

    public CrewDetails(String subject, String position, KSPDate boardTime) {
        this.subject = subject;
        this.position = position;
        this.boardTime = boardTime;
        this.expGained = 0;
    }

    public CrewDetails(List<String> fields) {
        this(fields.get(0), fields.get(1), new KSPDate(fields.get(2)));
        setExpGain(Float.parseFloat(fields.get(3)));

    }

    public String getSubject() {
        return subject;
    }

    public String getPosition() {
        return position;
    }

    public KSPDate getBoardTime() {
        return boardTime;
    }

    public float getExpGained() {
        return expGained;
    }

    public void setExpGain(float expGained) {
        this.expGained = expGained;
    }

    @Override
    public int getFieldCount() {
        return 4;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Subject";
            case 1 -> "Position";
            case 2 -> "Board time";
            case 3 -> "Experience gained";
            default -> "???";
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> subject + " Kerman";
            case 1 -> position;
            case 2 -> boardTime.toString();
            case 3 -> Float.toString(expGained);
            default -> "???";
        };
    }

    @Override
    public String getTextRepresentation() {
        return "Details: " + subject + " Kerman";
    }

    @Override
    public Collection<String> toStorableCollection() {
        List<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(subject);
        ret.add(position);
        ret.add(boardTime.toStorableString());
        ret.add(Float.toString(expGained));

        return ret;
    }
}

public class Mission extends KSPObject {

    public static final int ENCODE_FIELD_AMOUNT = 8; // ALWAYS ACCOUNT FOR DESCRIPTION
    private static final String DELIMITER = ":m:";

    private final String name;
    private final String vessel;
    private final long vesselId;
    private final Set<String> crew;
    private final Set<CrewDetails> crewDetails;
    private final KSPDate missionStart;
    private final List<MissionEvent> eventLog;


    /** Generates a mission from scratch.
     * @param name Mission name
     * @param vessel Name of the vessel
     * @param vesselId ID of the specific vessel instance
     * @param crew A map corresponding to the crew and their roles
     * @param missionStart Mission start date
     */
    public Mission(String name, String vessel, long vesselId, Map<Kerbal, String> crew, KSPDate missionStart) {
        this.name = name;
        this.vessel = vessel;
        this.vesselId = vesselId;
        this.crew = crew.keySet().stream().map(Kerbal::getName).collect(Collectors.toUnmodifiableSet());
        this.missionStart = missionStart;
        this.crewDetails = new HashSet<>();
        for (Kerbal k : crew.keySet()) crewDetails.add(new CrewDetails(k.getName(), crew.get(k), missionStart));
        eventLog = new LinkedList<>();
    }

    /** Generate a mission from a list of fields stored in persistence. =
     * @param fields List of fields
     */
    public Mission(LinkedList<String> fields) {
        this.name = fields.get(1);
        this.vessel = fields.get(2);
        this.vesselId = Long.parseLong(fields.get(3));
        this.crew = crewNamesFromString(fields.get(4));
        this.crewDetails = crewDetailsFromString(fields.get(5));
        this.missionStart = KSPDate.fromString(fields.get(6));
        this.eventLog = eventLogFromString(fields.get(7));
        setDescription(fields.get(0));
    }

    private Set<String> crewNamesFromString(String s) {
        return new HashSet<>(Arrays.asList(s.split(DELIMITER)));
    }

    private String crewNamesToString() {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (String name : crew) joiner.add(name);
        return joiner.toString();
    }

    private Set<CrewDetails> crewDetailsFromString(String s) {
        Set<CrewDetails> ret = new HashSet<>();
        String[] details = s.split(DELIMITER);
        for (String detail : details) {
            if (detail.length() != CrewDetails.ENCODE_FIELD_AMOUNT) continue;
            ret.add(new CrewDetails(Arrays.asList(detail.split(CrewDetails.DELIMITER))));
        }
        return ret;
    }

    private String crewDetailsToString() {
        StringJoiner j = new StringJoiner(DELIMITER);
        for (CrewDetails detail : crewDetails) {
            Collection<String> det = detail.toStorableCollection();
            StringJoiner subj = new StringJoiner(CrewDetails.DELIMITER);
            for (String d : det) subj.add(d);
            j.add(subj.toString());
        }
        return j.toString();
    }

    private List<MissionEvent> eventLogFromString(String s) {
       List<MissionEvent> ret = new LinkedList<>();
       String[] events = s.split(DELIMITER);
       for (String event : events) {
           List<String> fields = new LinkedList<>(Arrays.asList(event.split(MissionEvent.DELIMITER)));
           if (fields.size() != MissionEvent.ENCODE_FIELD_AMOUNT) {
               System.err.println("WARNING: Corrupt mission event found: " + event);
               continue;
           }
           ret.add(new MissionEvent(fields));
       }
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
        return joiner.toString();
    }


    public void kerbalRescued(Kerbal kerbal, KSPDate dateRescued) {
        this.crew.add(kerbal.getName());
        this.crewDetails.add(new CrewDetails(kerbal.getName(), "Rescued subject", dateRescued));
        logEvent(new MissionEvent(new LinkedList<>(Arrays.asList("", "", "", "", "")))); // TODO
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
        return Collections.unmodifiableSet(crew);
    }

    public CrewDetails getCrewDetails(Kerbal kerbal) {
        return crewDetails.stream()
                .filter(cd -> cd.getSubject().equals(kerbal))
                .findFirst()
                .orElse(null);
    }

    public float getExperienceGained(Kerbal kerbal) {
        return crewDetails.stream()
                .filter(cd -> cd.getSubject().equals(kerbal))
                .findFirst()
                .map(CrewDetails::getExpGained)
                .orElse(0.0f);
    }

    public List<MissionEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    public KSPDate getMissionStart() {
        return missionStart;
    }

    @Override
    public String getTextRepresentation() {
        return name;
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Name";
            case 1 -> "Vessel ID";
            default -> null;
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> name;
            case 1 -> Long.toString(vesselId);
            default -> null;
        };
    }

    @Override
    public Collection<String> toStorableCollection() {
        Collection<String> ret = super.toStorableCollection();

        ret.add(name);
        ret.add(vessel);
        ret.add(Long.toString(vesselId));
        ret.add(crewNamesToString());
        ret.add(crewDetailsToString());
        ret.add(missionStart.toStorableString());
        ret.add(eventLogToString());

        return ret;
    }
}
