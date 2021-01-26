package other;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Retarded class to define the different ways for a component to list itself
 */
public abstract class KSPObject implements Storable{

    protected static final String DELIMITER = "::";

    private String description;

    public abstract int getFieldCount();

    public abstract String getFieldName(int index);

    public abstract String getFieldValue(int index);

    public abstract String getTextRepresentation();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Collection<String> toStorableCollection() {
        LinkedList<String> ret = new LinkedList<>();
        ret.add(description);
        return ret;
    }
}
