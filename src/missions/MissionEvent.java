package missions;

import other.KSPObject;

import java.util.Collection;
import java.util.List;

public class MissionEvent extends KSPObject {

    public static final String DELIMITER = ":me:";
    public static final int ENCODE_FIELD_AMOUNT = 5;

    public MissionEvent(List<String> fields) {

    }

    // TODO

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public String getFieldName(int index) {
        return null;
    }

    @Override
    public String getFieldValue(int index) {
        return null;
    }

    @Override
    public String getTextRepresentation() {
        return null;
    }

    @Override
    public Collection<String> toStorableCollection() {
        return super.toStorableCollection();
    }
}
