package other.interfaces;

import other.util.Field;

import java.util.List;

/**
 * Works in pair with {@link Displayable} to indicate the object is, indeed, editable.
 */
public interface Editable extends Displayable {
    List<Field> getEditableFields();
    void setEditableField(String fieldName, String oldValue, String newValue);
}
