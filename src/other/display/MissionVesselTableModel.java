package other.display;

import vessels.Vessel;

import javax.swing.table.AbstractTableModel;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MissionVesselTableModel extends AbstractTableModel {

    private List<Vessel> vessels;

    public MissionVesselTableModel(Collection<Vessel> vessels) {
        this.vessels = vessels.stream().sorted(Comparator.comparing(Vessel::getId)).collect(Collectors.toList());
    }

    public List<Vessel> getVessels() {
        return Collections.unmodifiableList(vessels);
    }

    public void clear() {
        vessels.clear();
        fireTableDataChanged();
    }

    public void addVessel(Vessel vessel) {
        vessels.add(vessel);
        vessels.sort(Comparator.comparing(Vessel::getId));
        fireTableDataChanged();
    }

    public void removeVessel(Vessel vessel) {
        vessels.remove(vessel);
        fireTableDataChanged();
    }

    public Vessel getVessel(int row) {
        return vessels.get(row);
    }
    @Override
    public int getRowCount() {
        return vessels.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Vessel v = vessels.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> v.getName();
            case 1 -> v.getLocation();
            case 2 -> Long.toString(v.getId());
            default -> "???";
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "Name";
            case 1 -> "Location";
            case 2 -> "Identifier";
            default -> "???";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
