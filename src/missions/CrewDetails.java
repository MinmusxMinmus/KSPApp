package missions;

import kerbals.Kerbal;
import other.*;
import other.interfaces.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.util.Field;
import other.util.KSPDate;

import java.util.*;

public class CrewDetails extends KSPObject implements KSPObjectListener {

    public static final String DELIMITER = ":cd:";
    public static final int ENCODE_FIELD_AMOUNT = 5;

    private String name;
    private final String position;
    private final KSPDate boardTime;
    private float expGained;

    private Kerbal kerbal;

    public CrewDetails(ControllerInterface controller, String name, String position, KSPDate boardTime) {
        super(controller);
        this.name = name;
        this.position = position;
        this.boardTime = boardTime;
        this.expGained = 0.0f;
    }

    public CrewDetails(ControllerInterface controller, List<String> fields) {
        this(controller, fields.get(1), fields.get(2), new KSPDate(controller, fields.get(3)));
        setExpGain(Float.parseFloat(fields.get(4)));
        setDescription(fields.get(0));
    }

    public static CrewDetails fromString(ControllerInterface controller, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new CrewDetails(controller, new LinkedList<>(Arrays.asList(s.split(DELIMITER))));
    }

    public static String toString(CrewDetails crewDetails) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(crewDetails.name);
        joiner.add(crewDetails.getDescription() == null ? "(None)" : crewDetails.getDescription());
        joiner.add(crewDetails.position);
        joiner.add(crewDetails.boardTime.toStorableString());
        joiner.add(Float.toString(crewDetails.expGained));

        return joiner.toString();
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
    public void ready() {
        this.kerbal = getController().getKerbal(name);
        if (kerbal != null) kerbal.addEventListener(this);
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Name", name + " Kerman" + (kerbal.isKIA() ? " (KIA)" : "")));
        fields.add(new Field("Position", position));
        fields.add(new Field("Board time", boardTime.getTextRepresentation(false)));
        fields.add(new Field("Experience gained", Float.toString(expGained)));

        return fields;
    }

    @Override
    public String getTextRepresentation() {
        return name + " Kerman" +
                (kerbal != null && kerbal.isKIA() ? " (KIA)" : "") +
                position +
                ", boarded at " +
                boardTime.getTextRepresentation(false) +
                " (+" + expGained + "xp)" ;
    }

    @Override
    public Collection<String> toStorableCollection() {
        List<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(position);
        ret.add(boardTime.toStorableString());
        ret.add(Float.toString(expGained));

        return ret;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Kerbal deleted
        if (event.getSource() instanceof Kerbal k) {
            kerbal = null;
            name = "[REDACTED]";
        }
    }
}
