package gui;

import controller.GUIController;
import kerbals.Kerbal;
import missions.Mission;
import other.Listable;
import other.MainSearchCellRenderer;
import other.MainTableMultipleModel;
import vessels.Vessel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class MainScreen extends JFrame {

    private JPanel contentPane;
    private JPanel cardPanel;

    // Main window components
    private JButton mainEditButton;
    private JComboBox<String> mainEditComboBox;
    private JList<Listable> mainSearchResultList;
    private JPanel mainPanel;
    private JPanel mainInformationPanel;
    private JPanel mainTextPanel;
    private JLabel mainNotesLabel;
    private JTextArea mainDescriptionArea;
    private JPanel mainTablePanel;
    private JLabel mainTableNameLabel;
    private JScrollPane mainTableScrollPane;
    private JTable mainValuesTable;
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
    private JLabel missionCreationLabel;
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
    private JPanel astroPanel;
    private JPanel vesselPanel;
    private JLabel astroCreationLabel;
    private JPanel astroButtonsPanel;
    private JPanel astroDataPanel;
    private JButton astroOkButton;
    private JButton astroCancelButton;
    private JLabel astroNameLabel;
    private JPanel astroNamePanel;
    private JTextField astroNameTextField;
    private JCheckBox astroMaleCheckBox;
    private JComboBox astroJobComboBox;


    // Custom main components
    DefaultComboBoxModel<Listable> searchModel = new DefaultComboBoxModel<>();
    MainTableMultipleModel tableModel = new MainTableMultipleModel();

    private final GUIController controller;
    private final CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

    public MainScreen(GUIController controller) {
        this.controller = controller;

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                mainReset();
                ((MainScreen)e.getSource()).revalidate();
            }
        });

        // Window settings
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("KSP Autism App");
        setContentPane(contentPane);

        // Screen creation
        setupMainScreen();
    }

    /**
     * Main screen logic happens here.
     */
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
                    List<Kerbal> kerbals = controller.getKerbals();
                    searchModel.addAll(kerbals);
                }
                case "Missions" -> {
                    // Add missions to list
                    List<Mission> missions = controller.getMissions();
                    searchModel.addAll(missions);
                }
                case "Vessels" -> {
                    // Add vessels to list
                    List<Vessel> vessels = controller.getAllVessels();
                    searchModel.addAll(vessels);
                }
            }
        });

        // Create new listener
        mainCreateButton.addActionListener(e -> {
            String edit = (String) mainEditComboBox.getSelectedItem();
            if (edit == null) return;
            String screen = switch (edit) {
                case "Astronauts" -> "Astronaut creation";
                case "Missions" -> "Mission creation";
                case "Vessels" -> "Vessel creation";
                default -> "";
            };
            mainReset();
            MissionCreator mc = new MissionCreator(controller);
            mc.pack();
            mc.setVisible(true);
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

        // Search list listener
        mainSearchResultList.addListSelectionListener(e -> {
            Listable object = mainSearchResultList.getSelectedValue();
            if (object == null) return;
            tableModel.setItem(object);
            mainDescriptionArea.setText(object.getNotes() == null || object.getNotes().equals("")
                    ? "(No description given)"
                    : object.getNotes());
        });
    }

    /**
     * Initialization routine for the main screen. Called once, at window creation.
     */
    private void mainInitialize() {
        // Search result list requires a model to insert and remove data
        mainSearchResultList.setModel(searchModel);
        mainSearchResultList.setCellRenderer(new MainSearchCellRenderer());

        // Table requires a specialized model to display all types of data
        mainValuesTable.setModel(tableModel);
    }

    /**
     * Reset routine for the main screen. Called whenever the screen switches over to some other one.
     */
    private void mainReset() {
        // Default list: show astronauts
        searchModel.removeAllElements();
        searchModel.addAll(controller.getKerbals());

        // Default combo box: show astronauts
        mainEditComboBox.setSelectedItem("Astronauts");

        // Default description
        mainDescriptionArea.setText("Notes about the object will be shown here...");
        revalidate();
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
