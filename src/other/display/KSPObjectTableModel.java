package other.display;

import other.interfaces.Displayable;
import other.util.Field;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;

public class KSPObjectTableModel extends AbstractTableModel {

    private List<Field> fields = new LinkedList<>();

    public void setItem(Displayable item) {
        this.fields = item.getFields();
        fireTableDataChanged();
    }

    public void clear() {
        this.fields = new LinkedList<>();
        fireTableDataChanged();
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
        return (column == 0) ? "Name" : "Value" ;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Field field = fields.get(rowIndex);
        return columnIndex == 0 ? field.getName() : field.getValue();
    }
}
