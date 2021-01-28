package missions;

import other.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class MissionEvent extends KSPObject {

    public static final String DELIMITER = ":ME:";
    public static final int ENCODE_FIELD_AMOUNT = 4;

    // Persistent details
    private final String missionName;
    private final boolean oldInSpace;
    private final CelestialBody oldLocation;
    private final String details;

    // Dynamic details
    private Mission mission;


    private MissionEvent(ControllerInterface controller, String[] fields) {
        this(controller, fields[0], Boolean.parseBoolean(fields[1]), CelestialBody.valueOf(fields[2]), fields[3]);
    }

    public MissionEvent(ControllerInterface controller, String missionName, boolean oldInSpace, CelestialBody oldLocation, String details) {
        super(controller);
        this.missionName = missionName;
        this.oldInSpace = oldInSpace;
        this.oldLocation = oldLocation;
        this.details = details;
    }

    public static MissionEvent fromString(ControllerInterface c, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new MissionEvent(c, s.split(DELIMITER));
    }

    public static String toString(MissionEvent me) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(me.missionName);
        joiner.add(Boolean.toString(me.oldInSpace));
        joiner.add(me.oldLocation.name());
        joiner.add(me.details);

        return joiner.toString();
    }

    @Override
    public void ready() {
        mission = getController().getMission(missionName);
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Mission", missionName));
        fields.add(new Field("Previous location", (oldInSpace ? "Orbiting " : "Landed on ") + oldLocation.toString()));
        fields.add(new Field("Details", details));

        return fields;
    }

    @Override
    public String getTextRepresentation() {
        return null;
    }
}
