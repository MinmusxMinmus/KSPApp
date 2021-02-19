package other.interfaces;

import other.KSPObject;

public abstract class KSPObjectEvent {

    private final KSPObject source;

    public KSPObjectEvent(KSPObject source) {
        this.source = source;
    }

    public KSPObject getSource() {
        return source;
    }
}
