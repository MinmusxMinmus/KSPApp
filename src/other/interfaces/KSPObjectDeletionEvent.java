package other.interfaces;

import other.KSPObject;

public class KSPObjectDeletionEvent extends KSPObjectEvent {

    public KSPObjectDeletionEvent(KSPObject source) {
        super(source);
    }
}
