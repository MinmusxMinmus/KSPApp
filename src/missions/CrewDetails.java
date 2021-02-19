package missions;

import kerbals.Kerbal;
import other.*;
import controller.ControllerInterface;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;
import other.interfaces.KSPObjectUpdateEvent;
import other.util.Field;
import other.util.KSPDate;

import java.util.*;

public class CrewDetails extends KSPObject implements KSPObjectListener {

    public static final String DELIMITER = ":cd:";
    public static final int ENCODE_FIELD_AMOUNT = 4;

    private String name;
    private final String position;
    private final KSPDate boardTime;

    private Kerbal kerbal;

    public CrewDetails(ControllerInterface controller, String name, String position, KSPDate boardTime) {
        super(controller);
        this.name = name;
        this.position = position;
        this.boardTime = boardTime;
    }

    public CrewDetails(ControllerInterface controller, List<String> fields) {
        super(controller);
        setDescription(fields.get(0));
        this.name = fields.get(1);
        this.position = fields.get(2);
        this.boardTime = KSPDate.fromString(controller, fields.get(3));
    }

    public static CrewDetails fromString(ControllerInterface controller, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new CrewDetails(controller, new LinkedList<>(Arrays.asList(s.split(DELIMITER))));
    }

    public static String toString(CrewDetails crewDetails) {
        StringJoiner joiner = new StringJoiner(DELIMITER);

        joiner.add(crewDetails.getDescription() == null ? "(None)" : crewDetails.getDescription());
        joiner.add(crewDetails.name);
        joiner.add(crewDetails.position);
        joiner.add(crewDetails.boardTime.toStorableString());

        return joiner.toString();
    }

    public String getPosition() {
        return position;
    }

    public KSPDate getBoardTime() {
        return boardTime;
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
        fields.add(new Field("Board time", boardTime.toString(false, true)));

        return fields;
    }

    @Override
    public Collection<String> toStorableCollection() {
        List<String> ret = new LinkedList<>(super.toStorableCollection());

        ret.add(name);
        ret.add(position);
        ret.add(boardTime.toStorableString());

        return ret;
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
    public boolean isTextField(int index) {
        return false;
    }

    @Override
    public String getText(int index) {
        return null;
    }

    @Override
    public void onDeletion(KSPObjectDeletionEvent event) {
        // Kerbal deleted
        if (event.getSource() instanceof Kerbal k) {
            kerbal = null;
            name = "[REDACTED]";
        }
    }

    @Override
    public void onUpdate(KSPObjectUpdateEvent event) {
        if (event.getSource() instanceof Kerbal) {
            // Updating kerbal shared values
            switch (event.getFieldName()) {
                case Kerbal.NAME -> {
                    if (name.equals(event.getOldValue())) name = event.getNewValue();
                }
            }
        }
    }

    @Override
    public String toString() {
        return name + " Kerman" +
                (kerbal != null && kerbal.isKIA() ? " (KIA)" : "") +
                position +
                ", boarded at " +
                boardTime.toString(false, true);
    }

    @Override
    public List<Field> getEditableFields() {
        return new LinkedList<>();
    }

    @Override
    protected void setField(String fieldName, String value) {

    }
}
