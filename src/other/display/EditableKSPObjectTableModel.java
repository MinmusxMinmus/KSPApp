package other.display;

import other.KSPObject;
import other.util.Field;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class EditableKSPObjectTableModel extends AbstractTableModel {

    private final KSPObject object;
    private List<Field> fields;

    public EditableKSPObjectTableModel(KSPObject object) {
        this.object = object;
        this.fields = object.getEditableFields();
    }

    @Override
    public int getRowCount() {
        return fields.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "Name";
            case 1 -> "Value";
            default -> "???";
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Field f = fields.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> f.getName();
            case 1 -> f.getValue();
            default -> "???";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 1) return;
        Field editedField = fields.get(rowIndex);
        object.setField(editedField.getName(), (String)aValue);
        fields = object.getEditableFields();
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
