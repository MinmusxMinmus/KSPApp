package vessels;

import controller.ControllerInterface;
import other.util.Field;
import other.util.KSPDate;
import other.KSPObject;

import java.util.LinkedList;
import java.util.List;

public class IterationChange extends KSPObject {

    public static final int ENCODE_FIELD_AMOUNT = 3;
    public static final String DELIMITER = ":IC:";

    private final int iteration;
    private final String changes;
    private final KSPDate changeDate;

    public IterationChange(ControllerInterface controller, int iteration, String changes, KSPDate changeDate) {
        super(controller);
        this.iteration = iteration;
        this.changes = changes;
        this.changeDate = changeDate;
    }

    public IterationChange(ControllerInterface controller, String s) {
        super(controller);
        String[] three = s.split(DELIMITER);
        this.iteration = Integer.parseInt(three[0]);
        this.changes = three[1];
        this.changeDate = new KSPDate(getController(), three[2]);
    }

    public static IterationChange fromString(ControllerInterface controller, String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new IterationChange(controller, s);
    }

    public static String toString(IterationChange ic) {
        return ic.iteration + DELIMITER + ic.changes + DELIMITER + ic.changeDate.toStorableString();
    }

    public int getIteration() {
        return iteration;
    }

    public KSPDate getChangeDate() {
        return changeDate;
    }

    public String getChanges() {
        return changes;
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
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new LinkedList<>();

        fields.add(new Field("Iteration", "Mk" + iteration));
        fields.add(new Field("Date", changeDate.toString(true, false)));
        fields.add(new Field("Changes", changes));

        return fields;
    }

    @Override
    public String toString() {
        return "(" + changeDate.toString(true, true) + ") Mk" + iteration + ": " + changes;
    }


}
