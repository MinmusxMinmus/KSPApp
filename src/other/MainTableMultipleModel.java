package other;

import javax.swing.table.AbstractTableModel;

public class MainTableMultipleModel extends AbstractTableModel {

    private Listable item;

    public void setItem(Listable item) {
        this.item = item;
        fireTableDataChanged();
    }

    public Listable getItem() {
        return item;
    }

    @Override
    public int getRowCount() {
        if (item == null) return 0;
        return item.getFieldCount();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return (column == 0) ? "Name" : "Value" ;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (item == null) return null;
        if (columnIndex < 0 || columnIndex > 1) return null;
        return columnIndex == 0 ? item.getFieldName(rowIndex): item.getFieldValue(rowIndex);
    }
}
