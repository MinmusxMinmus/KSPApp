package other;

/**
 * Works in pair with {@link Displayable} to indicate the object is, indeed, editable.
 */
public interface Editable extends Displayable {
    boolean isEditable(int rowIndex);
    boolean setField(int index, Object object);
}
