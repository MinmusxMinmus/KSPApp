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

/**
 * Condecorations are simple "badges" given to brave kerbals who have gained an honorable mention in the space program.
 */
public class Condecoration extends KSPObject implements KSPObjectListener {

    private static final String DELIMITER = ":C:";
    private static final int ENCODE_FIELD_AMOUNT = 5;

    // Persistent fields
    /**
     * The name of the {@link Mission} where the condecoration was granted.
     */
    private String missionName;
    /**
     * The name of the {@link Kerbal} who received the award.
     */
    private String kerbalName;
    private final String title;
    /**
     * The {@link KSPDate} at which the condecoration was handed out.
     */
    private final KSPDate date;
    /**
     * The condecoration's description.
     */
    private final String mention;

    // Dynamic fields
    /**
     * The instance of the mission where this condecoration was given
     */
    private Mission mission;
    /**
     * The instance of the kerbal that received this condecoration
     */
    private Kerbal kerbal;


    public Condecoration(ControllerInterface c, String kerbalName, String missionName, KSPDate date, String title, String mention) {
        super(c);
        this.kerbalName = kerbalName;
        this.missionName = missionName;
        this.date = date;
        this.title = title;
        this.mention = mention;
    }

    public static String toString(Condecoration c) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(c.kerbalName);
        joiner.add(c.missionName);
        joiner.add(c.date.toStorableString());
        joiner.add(c.title);
        joiner.add(c.mention);

        return joiner.toString();
    }

    public static Condecoration fromString(ControllerInterface c, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        String[] split = s.split(DELIMITER);
        return new Condecoration(c, split[0], split[1], KSPDate.fromString(c, split[2]), split[3], split[4]);
    }

    // Overrides
    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Mission name", missionName));
        fields.add(new Field("Date", date.toString(true, true)));
        fields.add(new Field("Subject", kerbalName + " Kerman"));
        fields.add(new Field("Title", title));

        return fields;
    }

    @Override
    public String getDescription() {
        return mention;
    }

    @Override
    public boolean isComplexField(int index) {
        return index == 0 || index == 2;
    }

    @Override
    public KSPObject getComplexField(int index) {
        if (index == 0) return mission;
        if (index == 2) return kerbal;
        return null;
    }

    @Override
    public boolean isTextField(int index) {
        return index == 3;
    }

    @Override
    public String getText(int index) {
        if (index == 3) return title;
        return null;
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
        if (event.getSource() instanceof Mission m) missionName = "[REDACTED]";
        // Kerbal deletion
        if (event.getSource() instanceof Kerbal k) kerbalName = "[REDACTED]";
    }

    @Override
    public String toString() {
        return "(" + date.toString(false, true) + ") Awarded to "
                + kerbalName + " Kerman during " + missionName + ".\n" + title + ": " + mention;
    }
}
