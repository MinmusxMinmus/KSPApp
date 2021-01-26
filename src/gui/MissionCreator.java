package gui;

import controller.GUIController;
import kerbals.Kerbal;
import other.KSPDate;
import other.MainSearchCellRenderer;
import other.MissionAssignedTableModel;
import other.MissionFreeTableModel;
import vessels.Vessel;
import vessels.VesselConcept;
import vessels.VesselInstance;

import javax.swing.*;
import java.awt.event.ItemEvent;
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
    private JLabel descriptionLabel;
    private JTextArea descriptionTextArea;
    private JPanel vesselPanel;
    private JLabel activeVesselsLabel;
    private JComboBox<VesselInstance> activeVesselsComboBox;
    private JPanel crewPanel;
    private JPanel crewSelectionPanel;
    private JScrollPane availableCrewPane;
    private JTable crewFreeTable;
    private JLabel availableCrewLabel;
    private JLabel assignedCrewLabel;
    private JScrollPane assignedCrewPane;
    private JTable crewSelectedTable;
    private JLabel addCrewMembersLabel;
    private JPanel startPanel;
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
    private JLabel secondLabel;
    private JTextField secondTextField;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel descriptionAreaPanel;
    private JPanel secondPanel;
    private JComboBox<VesselConcept> vesselDesignsComboBox;
    private JLabel vesselDesignsLabel;
    private JCheckBox newVesselCheckBox;

    // Custom components
    private final MissionFreeTableModel freeModel; // TODO replace with abstract class and hierarchy
    private final MissionAssignedTableModel assignedModel = new MissionAssignedTableModel(new LinkedList<>());

    public MissionCreator(GUIController controller) {
        super(controller, MISSION_CREATOR);
        setContentPane(mainPanel);

        // Initializing
        activeVesselsComboBox.setRenderer(new MainSearchCellRenderer());
        vesselDesignsComboBox.setRenderer(new MainSearchCellRenderer());

        // Define table contents
        freeModel = new MissionFreeTableModel(controller.getKerbals().stream()
                .filter(Kerbal::isAvailable)
                .collect(Collectors.toSet()));
        crewFreeTable.setModel(freeModel);
        crewSelectedTable.setModel(assignedModel);

        // Vessel combo box reset
        for (VesselInstance vi : controller.getVesselInstances()) activeVesselsComboBox.addItem(vi);
        for (VesselConcept vc : controller.getVesselConcepts()) vesselDesignsComboBox.addItem(vc);

        // Good luck charm
        revalidate();

        // Setup events
        listenerSetup();
    }

    private void listenerSetup() {

        // New vessel listener
        newVesselCheckBox.addItemListener(e -> {
            switch (e.getStateChange()) {
                case ItemEvent.SELECTED -> {
                    vesselDesignsComboBox.setEnabled(true);
                    vesselDesignsLabel.setEnabled(true);
                }
                case ItemEvent.DESELECTED -> {
                    vesselDesignsComboBox.setEnabled(false);
                    vesselDesignsLabel.setEnabled(false);
                }
            }
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
                if (row >= 0)
                    if (row < assignedModel.getRowCount()) { // -1, because of the position text field TODO ????
                        Kerbal k = assignedModel.getKerbal(row);
                        assignedModel.removeKerbal(k);
                        freeModel.addKerbal(k);
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
            Vessel vessel = (Vessel) (vesselDesignsComboBox.isEnabled()
                                ? vesselDesignsComboBox.getSelectedItem()
                                : activeVesselsComboBox.getSelectedItem());
            String year = yearTextField.getText();
            String day = dayTextField.getText();
            String hour = hourTextField.getText();
            String minute = minuteTextField.getText();
            String second = secondTextField.getText();

            // Error checking
            if (name.strip().equals("")
                    || description.strip().equals("")
                    || vessel == null
                    || year.strip().equals("")
                    || day.strip().equals("")) {
                JOptionPane.showMessageDialog(mainPanel, "Please fill out all text fields!");
                return;
            }

            // Confirmation
            if (!ask("Create mission", "Are you sure you want to create this mission?")) return;

            // Date creation
            KSPDate date = (hour.strip().equals("") || minute.strip().equals("") || second.strip().equals(""))
                    ? new KSPDate(parseInt(year),
                    parseInt(day),
                    OffsetDateTime.now())
                    : new KSPDate(parseInt(year),
                    parseInt(day), parseInt(hour),
                    parseInt(minute),
                    parseInt(second),
                    OffsetDateTime.now());

            // Mission creation
            controller.addMission(name,
                    description,
                    vessel,
                    assignedModel.getCrew(),
                    date);

            // Form end
            dispose();
        });
    }
}
