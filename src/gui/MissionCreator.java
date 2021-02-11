package gui;

import controller.GUIController;
import kerbals.Kerbal;
import other.display.MissionAssignedKerbalTableModel;
import other.display.MissionKerbalTableModel;
import other.display.MissionVesselTableModel;
import other.util.KSPDate;
import vessels.Vessel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class MissionCreator extends KSPGUI {
    private JPanel mainPanel;
    private JLabel missionCreatorLabel;
    private JPanel dataPanel;
    private JPanel namePanel;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JPanel descriptionPanel;
    private JTextArea descriptionTextArea;
    private JPanel crewPanel;
    private JPanel crewSelectionPanel;
    private JScrollPane availableCrewPane;
    private JTable crewFreeTable;
    private JScrollPane assignedCrewPane;
    private JTable crewSelectedTable;
    private JTextField yearTextField;
    private JTextField dayTextField;
    private JTextField hourTextField;
    private JTextField minuteTextField;
    private JTextField secondTextField;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel descriptionAreaPanel;
    private JPanel creationDatePanel;
    private JPanel yearPanel;
    private JLabel yearLabel;
    private JPanel dayPanel;
    private JLabel dayLabel;
    private JPanel hourPanel;
    private JLabel hourLabel;
    private JPanel minutePanel;
    private JLabel minuteLabel;
    private JPanel secondPanel;
    private JLabel secondLabel;
    private JCheckBox preciseTimeCheckBox;
    private JPanel vesselPanel;
    private JPanel vesselSelectionPanel;
    private JScrollPane availableVesselsPane;
    private JScrollPane assignedVesselsPane;
    private JTable availableVesselsTable;
    private JTable assignedVesselsTable;

    // Custom components
    private final MissionKerbalTableModel freeModel = new MissionKerbalTableModel(controller.getKerbals().stream().filter(k -> !k.isKIA()).collect(Collectors.toSet()));
    private final MissionAssignedKerbalTableModel assignedModel = new MissionAssignedKerbalTableModel(new LinkedList<>());

    private final MissionVesselTableModel freeVModel = new MissionVesselTableModel(controller.getVessels());
    private final MissionVesselTableModel assignedVModel = new MissionVesselTableModel(new LinkedList<>());

    public MissionCreator(GUIController controller) {
        super(controller, MISSION_CREATOR);
        setContentPane(mainPanel);

        // Initializing
        // Define table contents
        crewFreeTable.setModel(freeModel);
        crewSelectedTable.setModel(assignedModel);

        availableVesselsTable.setModel(freeVModel);
        assignedVesselsTable.setModel(assignedVModel);

        // Good luck charm
        revalidate();

        // Setup events
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

        // Use vessel listener

        // Available kerbal table listener
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

        // Selected kerbal table listener
        crewSelectedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = crewSelectedTable.rowAtPoint(e.getPoint());
                int col = crewSelectedTable.columnAtPoint(e.getPoint());
                if (row >= 0)
                    if (row < assignedModel.getRowCount() // Is a row on the table
                            && col != assignedModel.getColumnCount() - 1 // Is not the last cell (Position in the mission)
                            && crewSelectedTable.getSelectedRow() == row) { // Double click
                        Kerbal k = assignedModel.getKerbal(row);
                        assignedModel.removeKerbal(k);
                        freeModel.addKerbal(k);
                    }
            }
        });

        // Available vessel table listener
        availableVesselsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = availableVesselsTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < freeVModel.getRowCount()) {
                    Vessel v = freeVModel.getVessel(row);
                    freeVModel.removeVessel(v);
                    assignedVModel.addVessel(v);
                }
            }
        });

        // Selected vessel table listener
        assignedVesselsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = assignedVesselsTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < assignedVModel.getRowCount()) {
                    Vessel v = assignedVModel.getVessel(row);
                    assignedVModel.removeVessel(v);
                    freeVModel.addVessel(v);
                }
            }
        });

        // Cancel listener
        cancelButton.addActionListener(e -> {
            // Confirmation
            if (ask("Cancel mission creation", "Are you sure you want to cancel?")) {
                dispose();
            }
        });

        // Confirm listener
        OKButton.addActionListener(e -> {
            // Read values from text fields and combo boxes
            String name = nameTextField.getText();
            String description = descriptionTextArea.getText();
            String year = yearTextField.getText();
            String day = dayTextField.getText();
            String hour = hourTextField.getText();
            String minute = minuteTextField.getText();
            String second = secondTextField.getText();

            // Error checking
            if (name.strip().equals("")
                    || description.strip().equals("")
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
            if (!ask("Create mission", "Are you sure you want to create this mission?")) return;

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

            // Mission creation
            controller.createMission(name,
                    description,
                    assignedModel.getCrew2(),
                    assignedVModel.getVessels(),
                    date);

            // Form end
            dispose();
        });
    }
}
