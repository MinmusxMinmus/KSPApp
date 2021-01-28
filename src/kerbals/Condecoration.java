package kerbals;

import missions.Mission;
import other.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class Condecoration extends KSPObject implements KSPObjectListener {

    private static final String DELIMITER = ":C:";
    private static final int ENCODE_FIELD_AMOUNT = 5;

    private final String missionName;
    private final String kerbalName;
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
        fields.add(new Field("Date", date.getTextRepresentation()));
        fields.add(new Field("Subject", kerbalName + " Kerman" + (kerbal == null ? "" : "(" + inactiveReason + ")")));
        fields.add(new Field("Mention", mention));

        return fields;
    }

    @Override
    public String getTextRepresentation() {
        return "(" +
                date.getTextRepresentation() +
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

    @Override
    public void ready() {
        mission = getController().getMission(missionName);
        mission.addEventListener(this);
        kerbal = getController().getKerbal(kerbalName);
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        if (event.getSource() instanceof Mission m) { // Classified mission
            mission = null;
            classifiedReason = event.getStatus();
        } else if (event.getSource() instanceof Kerbal k) { // Unavailable kerbal
            kerbal = null;
            inactiveReason = event.getStatus();
        }
    }
}
