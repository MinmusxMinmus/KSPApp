package other;

import controller.ControllerInterface;
import other.interfaces.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

public abstract class KSPObject implements Displayable, Editable {

    protected static final String DELIMITER = "::";
    private final ControllerInterface controller;
    private String description;
    private final Vector<KSPObjectListener> listeners = new Vector<>();


    public KSPObject(ControllerInterface controller) {
        this.controller = controller;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<String> toStorableCollection() {
        LinkedList<String> ret = new LinkedList<>();
        ret.add(description);
        return ret;
    }

    public ControllerInterface getController() {
        return controller;
    }

    public abstract boolean isComplexField(int index);

    public abstract KSPObject getComplexField(int index);

    public abstract boolean isTextField(int index);

    public abstract String getText(int index);

    public void addEventListener(KSPObjectListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(KSPObjectListener listener) {
        listeners.remove(listener);
    }

    public void fireDeletionEvent() {
        for (KSPObjectListener listener : listeners) listener.onDeletion(new KSPObjectDeletionEvent(this));
        listeners.clear();
    }

    public void fireUpdateEvent(String fieldName, String oldValue, String newValue) {
        for (KSPObjectListener listener : listeners) listener.onUpdate(new KSPObjectUpdateEvent(this, fieldName, oldValue, newValue));
    }

    /** Indicates to the object that the controller has finished loading every {@link KSPObject} in memory. This allows
     * the current object to utilize the {@link KSPObject#controller} methods.
     */
    public abstract void ready();

    @Override
    public void setEditableField(String fieldName, String oldValue, String newValue) {
        setField(fieldName, newValue);
        fireUpdateEvent(fieldName, oldValue, newValue);
    }

    protected abstract void setField(String fieldName, String value);

    // TODO all objects require a "value changed" event, for example if a kerbal changes name missions with him will die!
}
