package other;

import other.interfaces.ControllerInterface;
import other.interfaces.Displayable;
import other.interfaces.KSPObjectDeletionEvent;
import other.interfaces.KSPObjectListener;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

public abstract class KSPObject implements Displayable {

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


    public void addEventListener(KSPObjectListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(KSPObjectListener listener) {
        listeners.remove(listener);
    }

    public void fireDeletionEvent(String status) {
        for (KSPObjectListener listener : listeners) listener.onDeletion(new KSPObjectDeletionEvent(this, status));
    }

    /** Indicates to the object that the controller has finished loading every {@link KSPObject} in memory. This allows
     * the current object to utilize the {@link KSPObject#controller} methods.
     */
    public abstract void ready();
}
