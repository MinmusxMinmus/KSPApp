package missions;

import other.*;
import controller.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.KSPDate;
import other.util.Location;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class MissionEvent extends KSPObject implements KSPObjectListener {

    public static final String DELIMITER = ":ME:";
    public static final int ENCODE_FIELD_AMOUNT = 4;

    // Persistent details
    private String missionName;
    private final KSPDate date;
    private final Location location;
    private final String details;

    // Dynamic details
    private Mission mission;


    private MissionEvent(ControllerInterface controller, String[] fields) {
        this(controller, fields[0], KSPDate.fromString(controller, fields[1]), Location.fromString(fields[2]), fields[3]);
    }

    public MissionEvent(ControllerInterface controller, String missionName, KSPDate date, Location location, String details) {
        super(controller);
        this.missionName = missionName;
        this.date = date;
        this.location = location;
        this.details = details;
    }

    public static MissionEvent fromString(ControllerInterface c, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new MissionEvent(c, s.split(DELIMITER));
    }

    public static String toString(MissionEvent me) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(me.missionName);
        joiner.add(me.date.toStorableString());
        joiner.add(Location.toString(me.location));
        joiner.add(me.details);

        return joiner.toString();
    }

    public String getMissionName() {
        return missionName;
    }

    public KSPDate getDate() {
        return date;
    }

    public Location getLocation() {
        return location;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public boolean isComplexField(int index) {
        return false;
    }

    @Override
    public KSPObject getComplexField(int index) {
        return null;
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
        fields.add(new Field("Previous location", location == null ? "Unknown" : location.toString()));
        fields.add(new Field("Details", details));

        return fields;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deletion
        if (event.getSource() instanceof Mission) {
            missionName = "[REDACTED]";
            mission = null;
        }
    }

    @Override
    public String toString() {
        String shortened;
        if (details.length() <= 30) shortened = details;
        else shortened = details.substring(0, 25) + "...";

        return shortened + " (" + date.toString(false, true) + ")";
    }
}
