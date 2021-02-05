package other.display;

import kerbals.Kerbal;

import javax.swing.table.AbstractTableModel;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MissionTableModel extends AbstractTableModel {

    private final List<Kerbal> kerbalList;

    public MissionTableModel(Collection<Kerbal> kerbals) {
        this.kerbalList = kerbals.stream()
                .sorted(Comparator.comparing(Kerbal::getName))
                .collect(Collectors.toList());
    }

    public List<Kerbal> getKerbalList() {
        return kerbalList;
    }

    public void addKerbal(Kerbal kerbal) {
        kerbalList.add(kerbal);
        kerbalList.sort(Comparator.comparing(Kerbal::getName));
        fireTableDataChanged();
    }

    public void removeKerbal(Kerbal kerbal) {
        kerbalList.remove(kerbal);
        fireTableDataChanged();
    }

    public Kerbal getKerbal(int row) {
        return kerbalList.get(row);
    }

    public Set<Kerbal> getCrew() {
        return kerbalList.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int getRowCount() {
        return kerbalList.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Kerbal k = kerbalList.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> k.getName() + " Kerman";
            case 1 -> k.isMale() ? "Male" : "Female";
            case 2 -> k.getJob().toString();
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
            default -> "???";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
