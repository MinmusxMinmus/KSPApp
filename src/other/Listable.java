package other;

/**
 * Retarded interface to define the different ways for a component to list itself
 */
public interface Listable {


    int getFieldCount();

    String getFieldName(int index);

    String getFieldValue(int index);

    String getTextRepresentation();

    String getNotes();
}
