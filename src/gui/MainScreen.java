package gui;

import controller.GUIController;
import kerbals.Kerbal;
import missions.Mission;
import other.KSPObject;
import other.MainSearchCellRenderer;
import other.MainTableMultipleModel;
import vessels.Vessel;
import vessels.VesselConcept;
import vessels.VesselInstance;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class MainScreen extends KSPGUI {

    private static final String KERBAL_LIST = "Kerbals";
    private static final String MISSION_LIST = "Missions";
    private static final String VESSEL_CONCEPT_LIST = "Vessel concepts";
    private static final String VESSEL_INSTANCE_LIST = "Vessel instances";
    // TODO add new item: edit here

    private JPanel mainPanel;

    // Main window components
    private JButton editButton;
    private JComboBox<String> selectionComboBox;
    private JList<KSPObject> searchList;
    private JTextArea descriptionArea;
    private JTable valuesTable;
    private JButton createButton;
    private JPanel outputPanel;
    private JPanel textPanel;
    private JLabel descriptionLabel;
    private JScrollPane descriptionPanel;
    private JPanel tablePanel;
    private JLabel tableNameLabel;
    private JScrollPane tableScrollPane;
    private JPanel inputPanel;
    private JPanel buttonPanel;
    private JButton deleteButton;
    private JPanel searchPanel;
    private JScrollPane searchScrollPane;
    private JButton saveChangesButton;
    private JButton discardChangesButton;


    // Custom main components
    private final DefaultComboBoxModel<KSPObject> searchModel = new DefaultComboBoxModel<>();
    private final MainTableMultipleModel tableModel = new MainTableMultipleModel();
    private final DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
    private String currentSelection;

    public MainScreen(GUIController controller) {
        super(controller, MAIN_SCREEN);
        setContentPane(mainPanel);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                reset();
                ((MainScreen)e.getSource()).revalidate();
            }
        });

        // Adding choices to the combo box
        selectionComboBox.setModel(comboBoxModel);
        comboBoxModel.addElement(KERBAL_LIST);
        comboBoxModel.addElement(MISSION_LIST);
        comboBoxModel.addElement(VESSEL_CONCEPT_LIST);
        comboBoxModel.addElement(VESSEL_INSTANCE_LIST);
        // TODO add new item: edit here

        // Search result list requires a model to insert and remove data
        searchList.setModel(searchModel);
        searchList.setCellRenderer(new MainSearchCellRenderer());

        // Table requires a specialized model to display all types of data
        valuesTable.setModel(tableModel);

        reset();
        listenerSetup();
    }

    private void listenerSetup() {

        // Combo box list updater
        selectionComboBox.addActionListener(e -> {
            String edit = (String) selectionComboBox.getSelectedItem();
            if (edit == null || edit.equals(currentSelection)) return;

            currentSelection = edit;
            searchModel.removeAllElements();
            switch (edit) {
                case KERBAL_LIST -> {
                    // Add astronauts to list
                    List<Kerbal> kerbals = controller.getKerbals();
                    searchModel.addAll(kerbals);
                }
                case MISSION_LIST -> {
                    // Add missions to list
                    List<Mission> missions = controller.getMissions();
                    searchModel.addAll(missions);
                }
                case VESSEL_CONCEPT_LIST -> {
                    // Add vessel concepts to list
                    List<VesselConcept> vessels = controller.getVesselConcepts();
                    searchModel.addAll(vessels);
                }
                case VESSEL_INSTANCE_LIST -> {
                    // Add vessel concepts to list
                    List<VesselInstance> vessels = controller.getVesselInstances();
                    searchModel.addAll(vessels);
                }
                // TODO add new item: edit here
            }
        });

        // Create button listener
        createButton.addActionListener(e -> {
            String edit = (String) selectionComboBox.getSelectedItem();
            if (edit == null) return;

            KSPGUI window;
            String name = MAIN_SCREEN;

            switch (edit) {
                case KERBAL_LIST -> {
                    window = new KerbalCreator(controller);
                    name = KERBAL_CREATOR;
                }
                case MISSION_LIST -> {
                    window = new MissionCreator(controller);
                    name = MISSION_CREATOR;
                }
                case VESSEL_CONCEPT_LIST, VESSEL_INSTANCE_LIST -> {
                    window = new VesselCreator(controller);
                    name = VESSEL_CREATOR;
                }
                // TODO Add new item: edit here
                default -> window = new MainScreen(controller);
            }

            window.appear(name);
            reset();
        });

        // Edit button listener
        editButton.addActionListener(e -> {
            // TODO
        });

        // Delete button listener
        deleteButton.addActionListener(e -> {
            // TODO
        });

        // Search selection listener
        searchList.addListSelectionListener(e -> {
            KSPObject object = searchList.getSelectedValue();
            if (object == null) return;

            // Values
            tableModel.setItem(object);

            // Description
            if (object.getDescription() == null || object.getDescription().equals(""))
                descriptionArea.setText("(No description given)");
            else descriptionArea.setText(object.getDescription());
        });

        // Save listener
        saveChangesButton.addActionListener(e -> {
            if (controller.saveChanges()) {
                say("Changes succesfully saved!");
            } else say("There was an error while saving");

            reset();
        });

        // Discard listener
        discardChangesButton.addActionListener(e -> {
            boolean ask = ask("Discard", "You will lose everything you've changed since the last save, or since you" +
                    "opened the app.\nAre you sure you want to discard all changes?");
            if (!ask) return;
            controller.discard();
            say("Changes discarded");
            reset();
        });
    }

    /**
     * Reset routine for the main screen. Called whenever the screen switches over to some other one.
     */
    private void reset() {
        // Default list: show astronauts
        searchModel.removeAllElements();
        searchModel.addAll(controller.getKerbals());

        // Default combo box: show astronauts
        selectionComboBox.setSelectedItem(KERBAL_LIST);

        // Default description
        descriptionArea.setText("Notes about the item will be shown here...");
        revalidate();
    }
}
