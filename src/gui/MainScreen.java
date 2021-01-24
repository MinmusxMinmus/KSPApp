package gui;

import controller.GUIController;
import kerbals.Kerbal;
import vessels.Vessel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class MissionTableModel extends AbstractTableModel {

    private final List<Kerbal> kerbalList;

    public MissionTableModel(Collection<Kerbal> kerbals) {
        this.kerbalList = kerbals.stream()
                .sorted(Comparator.comparing(Kerbal::getName))
                .collect(Collectors.toList());
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

    @Override
    public int getRowCount() {
        return kerbalList.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Kerbal k = kerbalList.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> k.getName() + " Kerman";
            case 1 -> k.isMale() ? "Male" : "Female";
            case 2 -> k.getRole().toString();
            case 3 -> k.getLevel();
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
}

public class MainScreen extends JDialog {

    private JPanel contentPane;
    private JPanel cardPanel;
    private JLabel cardslmao;

    // Main window components
    private JButton mainMissionsButton;
    private JComboBox<String> mainEditModeComboBox;
    private JList<String> mainSearchResultList;
    private JPanel mainPanel;
    private JPanel mainInformationPanel;
    private JPanel mainTextPanel;
    private JLabel mainDescriptionLabel;
    private JTextArea descriptionArea;
    private JPanel mainTablePanel;
    private JLabel mainTableNameLabel;
    private JScrollPane mainTableScrollPane;
    private JTable valuesTable;
    private JPanel mainInputPanel;
    private JPanel mainButtonPanel;
    private JButton mainVesselsButton;
    private JButton mainAstronautsButton;
    private JButton mainRngButton;
    private JPanel mainOptionsPanel;
    private JScrollPane mainDescriptionPanel;
    private JScrollPane mainEditListPane;

    // Mission creation components
    private JPanel missionPanel;
    private JButton missionOkButton;
    private JButton missionCancelButton;
    private JPanel missionDataPanel;
    private JPanel missionNamePanel;
    private JTextField missionNameTextField;
    private JPanel missionVesselPanel;
    private JLabel missionVesselLabel;
    private JComboBox<Vessel> missionVesselComboBox;
    private JTable missionCrewFreeTable;
    private JLabel missionAvailableCrewLabel;
    private JLabel missionAssignedCrewLabel;
    private JTable missionCrewSelectedTable;
    private JLabel missionAddCrewMembersLabel;
    private JLabel missionDescriptionLabel;
    private JTextArea missionDescriptionTextArea;
    private JLabel missionCreationFormLabel;
    private JPanel missionButtonsPanel;
    private JPanel missionCrewPanel;
    private JPanel missionCrewSelectionPanel;
    private JScrollPane missionAvailableCrewPane;
    private JScrollPane missionAssignedCrewPane;
    private JPanel missionDescriptionPanel;
    private JLabel missionNameLabel;


    // Custom main components

    // Custom mission components
    private Set<Kerbal> missionKerbalSet;
    private MissionTableModel freeModel;
    private MissionTableModel assignedModel;

    private final GUIController guiController;
    private final CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

    public MainScreen(GUIController controller) {
        this.guiController = controller;

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentPane(contentPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setupMainScreen();

        setupMissionCreation();
    }

    private void setupMainScreen() {

        // Change ListModel<> to DefaultListModel<> to be able to use addElement()
        // TODO replace with a custom model
        DefaultListModel<String> model = new DefaultListModel<>();
        mainSearchResultList.setModel(model);

        // Button listeners
        mainMissionsButton.addActionListener(e -> {
            String option = (String) mainEditModeComboBox.getSelectedItem();
            if (option == null) return;

            guiController.getMissions().forEach(o -> model.addElement(o.getName()));
            model.addElement("test");

            // Switch to mission creation layout
            switch (option) {
                case "Edit" -> {
                    mainSearchResultList.getSelectionModel().addListSelectionListener(this::mainEdit);
                    mainSearchResultList.getSelectionModel().removeListSelectionListener(this::mainBrowse);
                    mainSearchResultList.getSelectionModel().removeListSelectionListener(this::mainDelete);
                }
                case "Browse" -> {
                    mainSearchResultList.getSelectionModel().addListSelectionListener(this::mainBrowse);
                    mainSearchResultList.getSelectionModel().removeListSelectionListener(this::mainEdit);
                    mainSearchResultList.getSelectionModel().removeListSelectionListener(this::mainDelete);
                }
                case "Delete" -> {
                    mainSearchResultList.getSelectionModel().addListSelectionListener(this::mainDelete);
                    mainSearchResultList.getSelectionModel().removeListSelectionListener(this::mainBrowse);
                    mainSearchResultList.getSelectionModel().removeListSelectionListener(this::mainEdit);
                }
                case "Create" -> cardLayout.next(cardPanel);
                default -> System.exit(1);
            }
            repaint();
        });
    }

    private void setupMissionCreation() {
        // Crew section preparations
        Set<Kerbal> kerbalSet = guiController.getKerbals().stream()
                .filter(Kerbal::isAvailable)
                .collect(Collectors.toSet());
        freeModel = new MissionTableModel(kerbalSet);
        assignedModel = new MissionTableModel(new LinkedList<>());
        // Inserting kerbals in table
        missionCrewFreeTable.setModel(freeModel);
        missionCrewSelectedTable.setModel(assignedModel);

        // Available table listener
        missionCrewFreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = missionCrewFreeTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    Kerbal k = freeModel.getKerbal(row);
                    freeModel.removeKerbal(k);
                    assignedModel.addKerbal(k);
                }
            }
        });

        // Selected table listener
        missionCrewSelectedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = missionCrewSelectedTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    Kerbal k = assignedModel.getKerbal(row);
                    assignedModel.removeKerbal(k);
                    freeModel.addKerbal(k);
                }
            }
        });

        // Cancel listener
        missionCancelButton.addActionListener(e -> {
            freeModel = new MissionTableModel(kerbalSet);
            assignedModel = new MissionTableModel(new LinkedList<>());
            cardLayout.previous(cardPanel);
        });

        // Confirm listener
        missionOkButton.addActionListener(e -> {
            String name = missionNameTextField.getText();
            String description = missionDescriptionTextArea.getText();
            Vessel vessel = (Vessel) missionVesselComboBox.getSelectedItem();
            if (name == null || description == null || vessel == null) {
                JOptionPane.showMessageDialog(cardPanel, "Please fill out all text fields!");
                return;
            }
            guiController.addMission(name, description, vessel, missionKerbalSet);
            guiController.markAssigned(missionKerbalSet);
            cardLayout.previous(cardPanel);
        });
    }

    private void mainEdit(ListSelectionEvent e) {

    }

    private void mainBrowse(ListSelectionEvent e) {

    }

    private void mainDelete(ListSelectionEvent e) {

    }
}
