package other.interfaces;

import other.KSPObject;

public class KSPObjectUpdateEvent extends KSPObjectEvent {

    private final String fieldName;
    private final String oldValue;
    private final String newValue;

    public KSPObjectUpdateEvent(KSPObject source, String fieldName, String oldValue, String newValue) {
        super(source);
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getFieldName() {
        return fieldName;
    }
}
