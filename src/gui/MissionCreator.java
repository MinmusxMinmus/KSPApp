package gui;

import controller.GUIController;
import kerbals.Kerbal;
import other.KSPDate;
import other.MainSearchCellRenderer;
import other.MissionAssignedTableModel;
import other.MissionFreeTableModel;
import vessels.Vessel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class MissionCreator extends JFrame {
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
    private JLabel vesselLabel;
    private JComboBox<Vessel> vesselComboBox;
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
    private JButton okButton;
    private JButton cancelButton;
    private JPanel contentPanel;
    private JPanel descriptionAreaPanel;
    private JPanel secondPanel;

    // Custom components
    private final GUIController controller;
    private MissionFreeTableModel freeModel;
    private MissionAssignedTableModel assignedModel;

    public MissionCreator(GUIController controller) {
        this.controller = controller;

        // Window settings
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Mission creator");
        setContentPane(contentPanel);

        vesselComboBox.setRenderer(new MainSearchCellRenderer());

        reset();

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
                if (row >= 0 && row < assignedModel.getRowCount() - 1) { // -1, because of the position text field
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
        okButton.addActionListener(e -> {
            // Read values from text fields and combo boxes
            String name = nameTextField.getText();
            String description = descriptionTextArea.getText();
            Vessel vessel = (Vessel) vesselComboBox.getSelectedItem();
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
                    LocalDate.now())
                    : new KSPDate(parseInt(year),
                    parseInt(day), parseInt(hour),
                    parseInt(minute),
                    parseInt(second),
                    LocalDate.now());

            // Proper mission creation
            controller.addMission(name,
                    description,
                    vessel,
                    assignedModel.getCrew(),
                    date);

            reset();
            dispose();
        });

    }

    /**
     * Reset routine, called whenever the screen switches over to some other one.
     */
    private void reset() {
        // Redefine table contents
        freeModel = new MissionFreeTableModel(controller.getKerbals().stream()
                .filter(Kerbal::isAvailable)
                .collect(Collectors.toSet()));
        assignedModel = new MissionAssignedTableModel(new LinkedList<>());
        crewFreeTable.setModel(freeModel);
        crewSelectedTable.setModel(assignedModel);

        // Empty text fields
        nameTextField.setText("");
        descriptionTextArea.setText("");
        yearTextField.setText("");
        dayTextField.setText("");
        hourTextField.setText("");
        minuteTextField.setText("");
        secondTextField.setText("");

        // Vessel combo box reset
        for (Vessel v : controller.getAllVessels()) vesselComboBox.addItem(v);
        revalidate();
    }

    /** Utility method to display a choice box with the desired message.
     * @param dialogTitle The window's name.
     * @param message The message displayed.
     * @return true if the user clicked on "Yes", false otherwise.
     */
    private boolean ask(String dialogTitle, String message) {
        int ret = JOptionPane.showOptionDialog(mainPanel,
                message,
                dialogTitle,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null);
        return ret == JOptionPane.YES_OPTION;
    }
}
