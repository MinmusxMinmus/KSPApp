package other.interfaces;

import other.util.Field;

import java.util.List;

/**
 * Retarded class to define the different ways for a component to list itself
 */
public interface Displayable {

    /** Returns a list containing all of the object's fields and their values.
     */
    List<Field> getFields();
}
