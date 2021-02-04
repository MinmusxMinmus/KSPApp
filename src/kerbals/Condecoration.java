package kerbals;

import missions.Mission;
import other.*;
import controller.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.KSPDate;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class Condecoration extends KSPObject implements KSPObjectListener {

    private static final String DELIMITER = ":C:";
    private static final int ENCODE_FIELD_AMOUNT = 5;

    private String missionName;
    private String kerbalName;
    private final KSPDate date;
    private final String mention;

    /**
     * The instance of the mission where this condecoration was given
     */
    private Mission mission; // Might be null, if the mission is deleted
    private String classifiedReason;
    /**
     * The instance of the kerbal that received this condecoration
     */
    private Kerbal kerbal; // Might be null, if the kerbal is deleted
    private String inactiveReason;


    public Condecoration(ControllerInterface c, String kerbalName, String missionName, KSPDate date, String mention) {
        super(c);
        this.kerbalName = kerbalName;
        this.missionName = missionName;
        this.date = date;
        this.mention = mention;
    }

    public static String toString(Condecoration c) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(c.kerbalName);
        joiner.add(c.missionName);
        joiner.add(c.date.toStorableString());
        joiner.add(c.mention);

        return joiner.toString();
    }

    public static Condecoration fromString(ControllerInterface c, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        String[] split = s.split(DELIMITER);
        return new Condecoration(c, split[0], split[1], KSPDate.fromString(c, split[2]), split[3]);
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Mission name", missionName + (mission != null ? "" : classifiedReason)));
        fields.add(new Field("Date", date.toString(true, true)));
        fields.add(new Field("Subject", kerbalName + " Kerman" + (kerbal == null ? "" : "(" + inactiveReason + ")")));
        fields.add(new Field("Mention", mention));

        return fields;
    }

    @Override
    public void ready() {
        mission = getController().getMission(missionName);
        if (mission != null) mission.addEventListener(this);
        kerbal = getController().getKerbal(kerbalName);
        if (kerbal != null) kerbal.addEventListener(this);
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Mission deletion
        if (event.getSource() instanceof Mission m) {
            mission = null;
            missionName = "[REDACTED]";
            classifiedReason = event.getStatus();
        }
        // Kerbal deletion
        if (event.getSource() instanceof Kerbal k) {
            kerbal = null;
            kerbalName = "[REDACTED]";
            inactiveReason = event.getStatus();
        }
    }

    @Override
    public String toString() {
        return "(" +
                date.toString(false, true) +
                ") " +
                missionName +
                (mission != null ? "" :"(" + classifiedReason + ")") +
                ", to " +
                kerbalName +
                " Kerman" +
                (inactiveReason == null ? "" : "(" + inactiveReason + ")") +
                ":\n" +
                mention;
    }
}
