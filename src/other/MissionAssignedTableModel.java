package other;

import kerbals.Kerbal;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.stream.Collectors;

public class MissionAssignedTableModel extends AbstractTableModel {

    private final List<Kerbal> kerbalList;
    private final List<String> positionList;

    public MissionAssignedTableModel(Collection<Kerbal> kerbals) {
        this.kerbalList = kerbals.stream()
                .sorted(Comparator.comparing(Kerbal::getName))
                .collect(Collectors.toList());
        this.positionList = new LinkedList<>();
        for (int i = 0; i != kerbalList.size(); i++) positionList.add("Undecided");
    }

    public void addKerbal(Kerbal kerbal) {
        kerbalList.add(kerbal);
        kerbalList.sort(Comparator.comparing(Kerbal::getName));
        positionList.add(kerbalList.indexOf(kerbal), "Undecided");
        fireTableDataChanged();
    }

    public void removeKerbal(Kerbal kerbal) {
        kerbalList.remove(kerbal);
        fireTableDataChanged();
    }

    public Kerbal getKerbal(int row) {
        return kerbalList.get(row);
    }

    public Map<Kerbal, String> getCrew() {
        Map<Kerbal, String> ret = new HashMap<>();
        for (int i = 0; i != kerbalList.size(); i++)
            ret.put(kerbalList.get(i), (String) getValueAt(i, 4));
        return Collections.unmodifiableMap(ret);
    }

    @Override
    public int getRowCount() {
        return kerbalList.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Kerbal k = kerbalList.get(rowIndex);
        String position = positionList.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> k.getName() + " Kerman";
            case 1 -> k.isMale() ? "Male" : "Female";
            case 2 -> k.getRole().toString();
            case 3 -> k.getLevel();
            case 4 -> position;
            default -> "???";
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "Name";
            case 1 -> "Gender";
            case 2 -> "Job";
            case 3 -> "Level";
            case 4 -> "Position";
            default -> "???";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 4;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        super.setValueAt(aValue, rowIndex, columnIndex);
        if (columnIndex != 4) return;
        String position = (String) aValue;
        positionList.set(rowIndex, position);
    }
}
