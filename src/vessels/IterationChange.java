package vessels;

import other.KSPDate;
import other.KSPObject;

import java.util.Collection;
import java.util.LinkedList;

public class IterationChange extends KSPObject {

    public static final int ENCODE_FIELD_AMOUNT = 3;
    public static final String DELIMITER = ":IC:";

    private final String changes;
    private final KSPDate changeDate;

    public IterationChange(String changes, KSPDate changeDate) {
        this.changes = changes;
        this.changeDate = changeDate;
    }

    public IterationChange(String s) {
        String[] two = s.split(DELIMITER);
        this.changes = two[0];
        this.changeDate = new KSPDate(two[1]);
    }

    public static IterationChange fromString(String s) {
        if (s.split(DELIMITER).length != ENCODE_FIELD_AMOUNT) return null;
        return new IterationChange(s);
    }

    public static String toString(IterationChange ic) {
        return ic.changes + DELIMITER + ic.changeDate.toStorableString();
    }

    public KSPDate getChangeDate() {
        return changeDate;
    }

    public String getChanges() {
        return changes;
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public String getFieldName(int index) {
        return switch (index) {
            case 0 -> "Changes";
            case 1 -> "Change date";
            default -> "???";
        };
    }

    @Override
    public String getFieldValue(int index) {
        return switch (index) {
            case 0 -> changes;
            case 1 -> changeDate.getTextRepresentation();
            default -> "???";
        };
    }

    @Override
    public String getTextRepresentation() {
        return "Iteration";
    }


}
