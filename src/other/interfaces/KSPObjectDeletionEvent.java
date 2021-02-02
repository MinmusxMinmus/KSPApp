package other.interfaces;

import other.KSPObject;

public class KSPObjectDeletionEvent{

    private final KSPObject source;
    private final String status;

    public KSPObjectDeletionEvent(KSPObject source) {
        this.source = source;
        this.status = null;
    }

    public KSPObjectDeletionEvent(KSPObject source, String status) {
        this.source = source;
        this.status = status;
    }

    public KSPObject getSource() {
        return source;
    }

    public String getStatus() {
        return status;
    }
}
