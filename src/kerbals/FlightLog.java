package kerbals;

import missions.Mission;
import other.*;
import controller.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class FlightLog extends KSPObject implements KSPObjectListener {

    private static final String DELIMITER = ":fl:";
    private static final int ENCODE_FIELD_AMOUNT = 1;

    private String missionName;

    private Mission mission;

    public FlightLog(ControllerInterface controller, String mission) {
        super(controller);
        this.missionName = mission;
    }

    public FlightLog(ControllerInterface controller, String[] fields) {
        super(controller);
        this.missionName = fields[0];
    }

    public static FlightLog fromString(ControllerInterface controller, String s) {
        String[] split = s.split(DELIMITER);
        if (split.length != ENCODE_FIELD_AMOUNT) return null;
        return new FlightLog(controller, split);
    }

    public static String toString(FlightLog log) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(log.missionName);

        return joiner.toString();
    }


    public String getMissionName() {
        return missionName;
    }

    @Override
    public boolean isComplexField(int index) {
        return true;
    }

    @Override
    public KSPObject getComplexField(int index) {
        return mission;
    }

    @Override
    public boolean isTextField(int index) {
        return false;
    }

    @Override
    public String getText(int index) {
        return null;
    }

    @Override
    public void ready() {
        mission = getController().getMission(missionName);
        if (mission != null) {
            missionName = "[REDACTED]";
            mission.addEventListener(this);
        }
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Mission", missionName));

        return fields;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deletion
        if (event.getSource() instanceof Mission) {
            mission = null;
            missionName = "[REDACTED]";
        }
    }

    @Override
    public String toString() {
        return "Participated in " + missionName;
    }
}
