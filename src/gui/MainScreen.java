package gui;

import controller.GUIController;
import other.interfaces.Editable;
import other.KSPObject;
import other.display.MainSearchCellRenderer;
import other.display.KSPObjectTableModel;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

public class MainScreen extends KSPGUI {

    private static final String KERBAL_LIST = "Kerbals";
    private static final String MISSION_LIST = "Missions";
    private static final String VESSEL_CONCEPT_LIST = "Vessel concepts";
    private static final String VESSEL_INSTANCE_LIST = "Vessel instances";
    private static final String CRASHED_INSTANCE_LIST = "Crashed vessels";
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
    private final DefaultListModel<KSPObject> searchModel = new DefaultListModel<>();
    private final KSPObjectTableModel tableModel = new KSPObjectTableModel();
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
        comboBoxModel.addElement(CRASHED_INSTANCE_LIST);
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

        // Focus listener
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                reset();
            }
        });

        // Combo box list updater
        selectionComboBox.addActionListener(e -> {
            String edit = (String) selectionComboBox.getSelectedItem();
            if (edit == null) return;

            currentSelection = edit;
            searchModel.removeAllElements();
            switch (edit) {
                case KERBAL_LIST -> // Add astronauts to list
                        searchModel.addAll(controller.getKerbals());
                case MISSION_LIST -> // Add missions to list
                        searchModel.addAll(controller.getMissions());
                case VESSEL_CONCEPT_LIST -> // Add vessel concepts to list
                        searchModel.addAll(controller.getConcepts());
                case VESSEL_INSTANCE_LIST -> // Add vessel instances to list
                        searchModel.addAll(controller.getInstances());
                case CRASHED_INSTANCE_LIST -> // Add crashed instances to list
                        searchModel.addAll(controller.getCrashedInstances());
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
                case VESSEL_CONCEPT_LIST -> {
                    window = new VesselCreator(controller);
                    name = VESSEL_CREATOR;
                }
                case VESSEL_INSTANCE_LIST, CRASHED_INSTANCE_LIST -> {
                    say("To make a physical vessel, create a mission for it");
                    return;
                }
                // TODO Add new item: edit here
                default -> window = new MainScreen(controller);
            }

            window.appear(name);
        });

        // Edit button listener
        editButton.addActionListener(e -> {
            KSPObject object = searchList.getSelectedValue();
            String s = (String) selectionComboBox.getSelectedItem();
            if (object != null && s != null) {
                EditWindow edit = new EditWindow("Editing " + s.toLowerCase(Locale.ROOT).substring(0, s.length()), (Editable) object, this);
                edit.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        reset();
                    }
                });
            }
        });

        // Delete button listener
        deleteButton.addActionListener(e -> {
            KSPObject object = searchList.getSelectedValue();
            String s = (String) selectionComboBox.getSelectedItem();
            if (object != null && s != null) {
                boolean ask = ask("Delete " + s.toLowerCase(Locale.ROOT),
                        "Are you sure you want to delete this "
                                + s.substring(0, s.length() - 1).toLowerCase(Locale.ROOT) + "?");
                if (!ask) return;
                String status;
                while (true) {
                    status = askString("Deletion reason", """
                            Please type the reason for the deletion.
                            Try to keep it short and concise, as it has to somewhat fit in the display tables.
                            Each category has its own way to interpret that reason, and because of that you should beaware of the different formats.
                            Kerbals: Reason of suspension (KIAs are managed by missions)
                            Missions: Reason of classification.
                            Vessel concepts: Reason of deletion
                            Vessel instances: Reason for no longer being tracked (Vessel destruction is handled by mission)""");
                    if (status != null) break;
                    say("Please input a reason!");
                }
                controller.delete(object, status);
                reset();
            }
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
        currentSelection = KERBAL_LIST;

        // Default description
        descriptionArea.setText("Notes about the item will be shown here...");
        revalidate();
    }
}
