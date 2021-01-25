package gui;

import controller.GUIController;
import kerbals.Kerbal;
import missions.Mission;
import other.KSPDate;
import other.MainSearchCellRenderer;
import vessels.Vessel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

class MissionAssignedTableModel extends AbstractTableModel {

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

class MissionFreeTableModel extends AbstractTableModel {

    private final List<Kerbal> kerbalList;

    public MissionFreeTableModel(Collection<Kerbal> kerbals) {
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

    public Set<Kerbal> getCrew() {
        return kerbalList.stream().collect(Collectors.toUnmodifiableSet());
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

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}

public class MainScreen extends JFrame {

    private JPanel contentPane;
    private JPanel cardPanel;

    // Main window components
    private JButton mainEditButton;
    private JComboBox<String> mainEditComboBox;
    private JList<Object> mainSearchResultList;
    private JPanel mainPanel;
    private JPanel mainInformationPanel;
    private JPanel mainTextPanel;
    private JLabel mainNotesLabel;
    private JTextArea mainDescriptionArea;
    private JPanel mainTablePanel;
    private JLabel mainTableNameLabel;
    private JScrollPane mainTableScrollPane;
    private JTable valuesTable;
    private JPanel mainInputPanel;
    private JPanel mainButtonPanel;
    private JButton mainCreateButton;
    private JButton mainDeleteButton;
    private JPanel mainOptionsPanel;
    private JScrollPane mainNotesPanel;
    private JScrollPane mainListScrollPane;

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
    private JTextField missionYearTextField;
    private JTextField missionDayTextField;
    private JTextField missionHourTextField;
    private JLabel missionYearLabel;
    private JLabel missionDayLabel;
    private JLabel missionHourLabel;
    private JTextField missionMinuteTextField;
    private JLabel missionMinuteLabel;
    private JPanel missionStartPanel;
    private JPanel missionYearPanel;
    private JPanel missionDayPanel;
    private JPanel missionHourPanel;
    private JPanel missionMinutePanel;
    private JTextField missionSecondTextField;
    private JLabel missionSecondLabel;


    // Custom main components
    boolean isCreation = false;
    DefaultComboBoxModel<Object> searchModel = new DefaultComboBoxModel<>();

    // Custom mission components
    private MissionFreeTableModel freeModel;
    private MissionAssignedTableModel assignedModel;

    private final GUIController guiController;
    private final CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

    public MainScreen(GUIController controller) {
        this.guiController = controller;

        // Window settings
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("KSP Autism App");

        setContentPane(contentPane);

        setupMainScreen();

        setupMissionCreation();
    }

    private void setupMainScreen() {

        mainInitialize();
        mainReset();

        // Combo box list updater
        mainEditComboBox.addActionListener(e -> {
            String edit = (String) mainEditComboBox.getSelectedItem();
            if (edit == null) return;
            searchModel.removeAllElements();
            switch (edit) {
                case "Astronauts" -> {
                    // Add astronauts to list
                    List<Kerbal> kerbals = guiController.getKerbals();
                    searchModel.addAll(kerbals);
                }
                case "Missions" -> {
                    // Add missions to list
                    List<Mission> missions = guiController.getMissions();
                    searchModel.addAll(missions);
                }
                case "Vessels" -> {
                    // Add vessels to list
                    List<Vessel> vessels = guiController.getAllVessels();
                    searchModel.addAll(vessels);
                }
            }
        });

        // Create new listener
        mainCreateButton.addActionListener(e -> {
            String edit = (String) mainEditComboBox.getSelectedItem();
            if (edit == null) return;
            switch (edit) {
                case "Astronauts" -> cardLayout.show(cardPanel, "Astronaut creation");
                case "Missions" -> cardLayout.show(cardPanel, "Mission creation");
                case "Vessels" -> cardLayout.show(cardPanel, "Vessel creation");
            }
        });

        // Edit listener
        mainEditButton.addActionListener(e -> {
            String edit = (String) mainEditComboBox.getSelectedItem();
            if (edit == null) return;
            switch (edit) {
                case "Astronauts" -> {

                }
                case "Missions" -> {

                }
                case "Vessels" -> {

                }
            }
        });
    }

    /**
     * Initialization routine for the main screen. Called once, at window creation.
     */
    private void mainInitialize() {
        // Search result list requires a model to insert and remove data
        mainSearchResultList.setModel(searchModel);
        mainSearchResultList.setCellRenderer(new MainSearchCellRenderer());
    }

    /**
     * Reset routine for the main screen. Called whenever the screen switches over to some other one.
     */
    private void mainReset() {
        // Default list: show astronauts
        searchModel.addAll(guiController.getKerbals());
    }

    /**
     * Mission creation screen logic happens here.
     */
    private void setupMissionCreation() {
        // Basic setup
        missionInitialize();
        missionReset();

        // Available table listener
        missionCrewFreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = missionCrewFreeTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < freeModel.getRowCount()) {
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
                if (row >= 0 && row < assignedModel.getRowCount() - 1) { // -1, because of the position text field
                    Kerbal k = assignedModel.getKerbal(row);
                    assignedModel.removeKerbal(k);
                    freeModel.addKerbal(k);
                }
            }
        });

        // Cancel listener
        missionCancelButton.addActionListener(e -> {
            // Confirmation
            if (ask("Cancel mission creation", "Are you sure you want to cancel?")) {
                missionReset();
                cardLayout.show(cardPanel, "main screen");
            }
        });

        // Confirm listener
        missionOkButton.addActionListener(e -> {
            // Read values from text fields and combo boxes
            String name = missionNameTextField.getText();
            String description = missionDescriptionTextArea.getText();
            Vessel vessel = (Vessel) missionVesselComboBox.getSelectedItem();
            String year = missionYearTextField.getText();
            String day = missionDayTextField.getText();
            String hour = missionHourTextField.getText();
            String minute = missionMinuteTextField.getText();
            String second = missionSecondTextField.getText();

            // Error checking
            if (name.strip().equals("")
                    || description.strip().equals("")
                    || vessel == null
                    || year.strip().equals("")
                    || day.strip().equals("")) {
                JOptionPane.showMessageDialog(cardPanel, "Please fill out all text fields!");
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
            guiController.addMission(name,
                    description,
                    vessel,
                    assignedModel.getCrew(),
                    date);

            // Resets the mission creation screen
            missionReset();

            // Returns to the main screen
            cardLayout.show(cardPanel, "main screen");
        });
    }

    /**
     * Initialization routine for the mission creation screen. Called once, at window creation.
     */
    private void missionInitialize() {
        missionVesselComboBox.setRenderer(new MainSearchCellRenderer());
    }

    /**
     * Reset routine for the mission creation screen. Called whenever the screen switches over to some other one.
     */
    private void missionReset() {
        // Redefine table contents
        freeModel = new MissionFreeTableModel(guiController.getKerbals().stream()
                .filter(Kerbal::isAvailable)
                .collect(Collectors.toSet()));
        assignedModel = new MissionAssignedTableModel(new LinkedList<>());
        missionCrewFreeTable.setModel(freeModel);
        missionCrewSelectedTable.setModel(assignedModel);

        // Empty text fields
        missionNameTextField.setText("");
        missionDescriptionTextArea.setText("");
        missionYearTextField.setText("");
        missionDayTextField.setText("");
        missionHourTextField.setText("");
        missionMinuteTextField.setText("");
        missionSecondTextField.setText("");

        // Vessel combo box reset
        for (Vessel v : guiController.getAllVessels()) missionVesselComboBox.addItem(v);
    }

    /** Utility method to display a choice box with the desired message.
     * @param dialogTitle The window's name.
     * @param message The message displayed.
     * @return true if the user clicked on "Yes", false otherwise.
     */
    private boolean ask(String dialogTitle, String message) {
        int ret = JOptionPane.showOptionDialog(cardPanel,
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
