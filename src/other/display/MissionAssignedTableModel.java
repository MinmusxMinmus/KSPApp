package other.display;

import kerbals.Kerbal;

import java.util.*;

public class MissionAssignedTableModel extends MissionTableModel {

    private final List<String> positionList;

    public MissionAssignedTableModel(Collection<Kerbal> kerbals) {
        super(kerbals);
        this.positionList = new LinkedList<>();
        for (int i = 0; i != getKerbalList().size(); i++) positionList.add("Undecided");
    }

    public void addKerbal(Kerbal kerbal) {
        super.addKerbal(kerbal);
        positionList.add(getKerbalList().indexOf(kerbal), "Unknown position");
        fireTableDataChanged();
    }

    public void removeKerbal(Kerbal kerbal) {
        super.removeKerbal(kerbal);
        int index = getKerbalList().indexOf(kerbal);
        positionList.remove(index);
        fireTableDataChanged();
    }

    public Map<Kerbal, String> getCrew2() {
        Map<Kerbal, String> ret = new HashMap<>();
        for (int i = 0; i != getKerbalList().size(); i++)
            ret.put(getKerbalList().get(i), (String) getValueAt(i, super.getColumnCount() + 1));
        return Collections.unmodifiableMap(ret);
    }

    @Override
    public int getColumnCount() {
        return super.getColumnCount() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex != super.getColumnCount()) return super.getValueAt(rowIndex, columnIndex);
        return positionList.get(rowIndex);
    }

    @Override
    public String getColumnName(int column) {
        if (column != super.getColumnCount()) return super.getColumnName(column);
        return "Position";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == super.getColumnCount();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        super.setValueAt(aValue, rowIndex, columnIndex);
        if (columnIndex != super.getColumnCount()) return;
        String position = (String) aValue;
        positionList.set(rowIndex, position);
    }
}
