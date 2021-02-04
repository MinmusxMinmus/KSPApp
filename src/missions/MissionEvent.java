package missions;

import other.*;
import controller.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.Location;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class MissionEvent extends KSPObject implements KSPObjectListener {

    public static final String DELIMITER = ":ME:";
    public static final int ENCODE_FIELD_AMOUNT = 3;

    // Persistent details
    private String missionName;
    private final Location oldLocation; // TODO replace with Location object
    private final String details;

    // Dynamic details
    private Mission mission;


    private MissionEvent(ControllerInterface controller, String[] fields) {
        this(controller, fields[0], Location.fromString(fields[1]), fields[2]);
    }

    public MissionEvent(ControllerInterface controller, String missionName, Location oldLocation, String details) {
        super(controller);
        this.missionName = missionName;
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
        joiner.add(Location.toString(me.oldLocation));
        joiner.add(me.details);

        return joiner.toString();
    }

    @Override
    public void ready() {
        mission = getController().getMission(missionName);
        if (mission != null) mission.addEventListener(this);
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Mission", missionName));
        fields.add(new Field("Previous location", oldLocation == null ? "Unknown" : oldLocation.toString()));
        fields.add(new Field("Details", details));

        return fields;
    }

    @Override
    public String getTextRepresentation() {
        return null;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deletion
        if (event.getSource() instanceof Mission) {
            missionName = "[REDACTED]";
            mission = null;
        }
    }
}
