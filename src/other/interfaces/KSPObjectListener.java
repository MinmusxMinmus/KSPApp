package other.interfaces;

public interface KSPObjectListener {
    void onDeletion(KSPObjectDeletionEvent event);
    void onUpdate(KSPObjectUpdateEvent event);
}
