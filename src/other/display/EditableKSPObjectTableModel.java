package other.display;

import other.display.KSPObjectTableModel;
import other.interfaces.Editable;

public class EditableKSPObjectTableModel extends KSPObjectTableModel {

    private final KSPObjectTableModel model = new KSPObjectTableModel();
    private Editable object;

    public EditableKSPObjectTableModel(Editable object) {
        super();
        this.object = object;
        model.setItem(object);
    }

    public Editable getItem() {
        return object;
    }

    @Override
    public int getRowCount() {
        return model.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return model.getColumnCount();
    }

    @Override
    public String getColumnName(int column) {
        return model.getColumnName(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return model.getValueAt(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 && !object.isEditable(rowIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 2) return;
        object.setField(rowIndex, aValue);
        model.setItem(object);
        fireTableCellUpdated(rowIndex, columnIndex);
        model.fireTableCellUpdated(rowIndex, columnIndex);
    }
}
