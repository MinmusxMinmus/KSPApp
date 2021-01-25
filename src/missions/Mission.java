package missions;

import kerbals.Kerbal;
import other.KSPDate;
import other.Listable;
import vessels.Vessel;
import vessels.VesselDestination;

import java.util.*;

class CrewDetails {

    private final Kerbal subject;
    private final String position;
    private final KSPDate boardTime;
    private final float expGained;

    public CrewDetails(Kerbal subject, String position, KSPDate boardTime) {
        this.subject = subject;
        this.position = position;
        this.boardTime = boardTime;
        this.expGained = 0;
    }

    public Kerbal getSubject() {
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

    public void expGain(VesselDestination location) {
    }
}
public class Mission implements Listable {

    private final String name;
    private final Vessel vessel;
    private final Set<Kerbal> crew;
    private final Set<CrewDetails> crewDetails = new HashSet<>();
    private final KSPDate missionStart;
    private final List<MissionEvent> eventLog = new LinkedList<>();

    private String notes;


    public Mission(String name, String description, Vessel vessel, Map<Kerbal, String> crew, KSPDate missionStart) {
        this.name = name;
        this.notes = description;
        this.vessel = vessel;
        this.crew = crew.keySet();
        this.missionStart = missionStart;
        for (Kerbal k : crew.keySet()) crewDetails.add(new CrewDetails(k, crew.get(k), missionStart));
    }

    public void kerbalRescued(Kerbal kerbal, KSPDate dateRescued) {
        this.crew.add(kerbal);
        this.crewDetails.add(new CrewDetails(kerbal, "Rescued subject", dateRescued));
        logEvent(new MissionEvent()); // TODO
    }

    public void logEvent(MissionEvent event) {
        eventLog.add(event);
    }

    public String getName() {
        return name;
    }

    public Vessel getVessel() {
        return vessel;
    }

    public Set<Kerbal> getCrew() {
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
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getTextRepresentation() {
        return name;
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Name";
            default -> null;
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> name;
            default -> null;
        };
    }
}
