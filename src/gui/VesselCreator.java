package gui;

import controller.GUIController;
import kerbals.Kerbal;
import other.display.MissionKerbalTableModel;
import other.util.CelestialBody;
import other.util.KSPDate;
import other.util.Location;
import vessels.Concept;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class VesselCreator extends KSPGUI {
    private JLabel vesselCreatorLabel;
    private JPanel dataPanel;
    private JPanel namePanel;
    private JComboBox<Concept> conceptComboBox;
    private JPanel creationDatePanel;
    private JPanel yearPanel;
    private JLabel yearLabel;
    private JTextField yearTextField;
    private JPanel dayPanel;
    private JLabel dayLabel;
    private JTextField dayTextField;
    private JPanel hourPanel;
    private JLabel hourLabel;
    private JTextField hourTextField;
    private JPanel minutePanel;
    private JLabel minuteLabel;
    private JTextField minuteTextField;
    private JPanel secondPanel;
    private JLabel secondLabel;
    private JTextField secondTextField;
    private JCheckBox preciseTimeCheckBox;
    private JPanel descriptionPanel;
    private JTextArea descriptionTextArea;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private JComboBox<CelestialBody> locationComboBox;
    private JPanel crewPanel;
    private JPanel crewSelectionPanel;
    private JScrollPane availableCrewPane;
    private JTable crewFreeTable;
    private JScrollPane assignedCrewPane;
    private JTable crewSelectedTable;
    private JLabel conceptLabel;
    private JLabel locationLabel;
    private JCheckBox inSpaceCheckBox;

    private final DefaultComboBoxModel<CelestialBody> locationModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<Concept> conceptModel = new DefaultComboBoxModel<>();
    private final MissionKerbalTableModel freeModel = new MissionKerbalTableModel(
            controller.getKerbals().stream()
                    .filter(k -> k.getLocation().landedAt(CelestialBody.KERBIN))
                    .filter(k -> !k.isKIA())
                    .collect(Collectors.toSet()));
    private final MissionKerbalTableModel assignedModel = new MissionKerbalTableModel(new LinkedList<>());

    public VesselCreator(GUIController controller) {
        super(controller, VESSEL_CREATOR);
        setContentPane(mainPanel);

        conceptComboBox.setModel(conceptModel);
        locationComboBox.setModel(locationModel);

        conceptModel.addAll(controller.getConcepts());
        locationModel.addAll(Arrays.asList(CelestialBody.values()));
        locationModel.setSelectedItem(CelestialBody.KERBIN);

        // Crew tables
        crewFreeTable.setModel(freeModel);
        crewSelectedTable.setModel(assignedModel);

        listenerSetup();
    }

    private void listenerSetup() {

        // Precise time listener
        preciseTimeCheckBox.addItemListener(e -> {
            hourTextField.setEnabled(preciseTimeCheckBox.isSelected());
            minuteTextField.setEnabled(preciseTimeCheckBox.isSelected());
            secondTextField.setEnabled(preciseTimeCheckBox.isSelected());
            hourLabel.setEnabled(preciseTimeCheckBox.isSelected());
            minuteLabel.setEnabled(preciseTimeCheckBox.isSelected());
            secondLabel.setEnabled(preciseTimeCheckBox.isSelected());
        });

        // Selected celestial body listener
        locationComboBox.addActionListener(e -> {
            CelestialBody celestialBody = (CelestialBody) locationComboBox.getSelectedItem();
            boolean inSpace = inSpaceCheckBox.isSelected();
            updateKerbalTables(inSpace, celestialBody);
        });

        // In space listener
        inSpaceCheckBox.addActionListener(e -> {
            CelestialBody celestialBody = (CelestialBody) locationComboBox.getSelectedItem();
            boolean inSpace = inSpaceCheckBox.isSelected();
            updateKerbalTables(inSpace, celestialBody);
        });

        // Available table listener
        crewFreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = crewFreeTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < freeModel.getRowCount()) {
                    Kerbal k = freeModel.getKerbal(row);
                    freeModel.removeKerbal(k);
                    assignedModel.addKerbal(k);
                }
            }
        });

        // Selected table listener
        crewSelectedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = crewSelectedTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < assignedModel.getRowCount()) {
                    Kerbal k = assignedModel.getKerbal(row);
                    assignedModel.removeKerbal(k);
                    freeModel.addKerbal(k);
                }
            }
        });

        // Cancel listener
        cancelButton.addActionListener(e -> {
            // Confirmation
            if (ask("Cancel vessel creation", "Are you sure you want to cancel?")) {
                dispose();
            }
        });

        // Confirm listener
        OKButton.addActionListener(e -> {
            // Read values from text fields and combo boxes
            Concept concept = (Concept) conceptComboBox.getSelectedItem();
            CelestialBody celestialBody = (CelestialBody) locationComboBox.getSelectedItem();
            boolean inSpace = inSpaceCheckBox.isSelected();
            String description = descriptionTextArea.getText();
            String year = yearTextField.getText();
            String day = dayTextField.getText();
            String hour = hourTextField.getText();
            String minute = minuteTextField.getText();
            String second = secondTextField.getText();
            Set<Kerbal> crew = assignedModel.getCrew();

            // Error checking
            if (concept == null) {
                say("Please select a concept!");
                return;
            }

            if (description.strip().equals("")
                    || year.strip().equals("")
                    || day.strip().equals("")
                    || preciseTimeCheckBox.isSelected() && (
                    hour.strip().equals("")
                            || minute.strip().equals("")
                            || second.strip().equals(""))) {
                say("Please fill out all text fields!");
                return;
            }

            // Confirmation
            if (!ask("Create vessel", "Are you sure you want to create this vessel?")) return;

            // Date creation
            KSPDate date = (!preciseTimeCheckBox.isSelected())
                    ? new KSPDate(controller,
                    parseInt(year),
                    parseInt(day),
                    OffsetDateTime.now())
                    : new KSPDate(controller,
                    parseInt(year),
                    parseInt(day), parseInt(hour),
                    parseInt(minute),
                    parseInt(second),
                    OffsetDateTime.now());

            // Location creation
            Location location = new Location(inSpace, celestialBody);

            // Vessel creation
            controller.createVessel(concept, location, date, crew);

            // Form end
            dispose();
        });
    }

    private void updateKerbalTables(boolean inSpace, CelestialBody celestialBody) {
        freeModel.clear();
        assignedModel.clear();
        for (Kerbal k : controller.getKerbals().stream()
                .filter(k -> k.getLocation().getCelestialBody().equals(celestialBody) && k.getLocation().isInSpace() == inSpace)
                .collect(Collectors.toSet())) freeModel.addKerbal(k);
    }
}
