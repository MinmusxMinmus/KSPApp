package gui;

import controller.GUIController;
import missions.Mission;
import other.KSPObject;
import other.display.KSPObjectTableModel;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

public class MainScreen extends KSPGUI {

    private static final String KERBAL_LIST = "Kerbals";
    private static final String MISSION_LIST = "Missions";
    private static final String VESSEL_CONCEPT_LIST = "Vessel concepts";
    private static final String VESSEL_INSTANCE_LIST = "Vessel instances";
    private static final String CRASHED_INSTANCE_LIST = "Crashed vessels";
    private static final String ARCHIVE_LIST = "Mission archives";
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
    private JButton updateButton;
    private JButton missionEditorButton;


    // Custom main components
    private final DefaultListModel<KSPObject> searchModel = new DefaultListModel<>();
    private final KSPObjectTableModel tableModel = new KSPObjectTableModel();
    private final DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
    private String currentSelection;
    private KSPObject currentSelectedObject;

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
        comboBoxModel.addElement(ARCHIVE_LIST);
        // TODO add new item: edit here

        // Search result list requires a model to insert and remove data
        searchList.setModel(searchModel);

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

            descriptionArea.setText("Notes about the item will be shown here...");
            missionEditorButton.setEnabled(false);
            currentSelection = edit;
            searchModel.removeAllElements();
            switch (edit) {
                case KERBAL_LIST -> // Add astronauts to list
                        searchModel.addAll(controller.getKerbals());
                case MISSION_LIST -> // Add missions to list
                        {
                            searchModel.addAll(controller.getMissions());
                            missionEditorButton.setEnabled(true);
                        }
                case VESSEL_CONCEPT_LIST -> // Add vessel concepts to list
                        searchModel.addAll(controller.getConcepts());
                case VESSEL_INSTANCE_LIST -> // Add vessel instances to list
                        searchModel.addAll(controller.getVessels());
                case CRASHED_INSTANCE_LIST -> // Add crashed instances to list
                        searchModel.addAll(controller.getCrashedVessels());
                case ARCHIVE_LIST -> // Add mission archives to list
                        searchModel.addAll(controller.getArchives());
                // TODO add new item: edit here
            }
        });

        // Create button listener
        createButton.addActionListener(e -> {
            String edit = (String) selectionComboBox.getSelectedItem();
            if (edit == null) return;

            KSPGUI window;
            String name;
            descriptionArea.setText("Notes about the item will be shown here...");

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
                    window = new ConceptCreator(controller);
                    name = VESSEL_CREATOR;
                }
                case VESSEL_INSTANCE_LIST -> {
                    window = new VesselCreator(controller);
                    name = VESSEL_CREATOR;
                }
                case CRASHED_INSTANCE_LIST -> {
                    say("You can't create a crashed vessel!");
                    return;
                }
                case ARCHIVE_LIST -> {
                    say("You can't create an archived mission!");
                    return;
                }
                // TODO Add new item: edit here
                default -> {
                    return;
                }
            }

            window.appear();
            clear();
        });

        // Edit button listener
        editButton.addActionListener(e -> {
            KSPObject object = searchList.getSelectedValue();
            String s = (String) selectionComboBox.getSelectedItem();
            if (object != null && s != null) {
                ObjectEditor editor = new ObjectEditor(controller, object);
                editor.appear();
                clear();
            }
        });

        missionEditorButton.addActionListener(e -> {
            Mission m = (Mission) searchList.getSelectedValue();
            if (m == null) {
                say("Please select a mission");
                return;
            }
            KSPGUI editor = new MissionUpdater(controller, "Mission editor", m);
            editor.appear();
            clear();
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
                controller.delete(object);
                reset();
            }
        });

        // Update button listener
        updateButton.addActionListener(e -> controller.ready());

        // Search selection listener
        searchList.addListSelectionListener(e -> {
            KSPObject object = searchList.getSelectedValue();
            if (object == null) return;

            this.currentSelectedObject = object;

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

        // Table detail listener
        valuesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (currentSelectedObject == null) return;
                int row = valuesTable.rowAtPoint(e.getPoint());
                if (row < 0 && row >= tableModel.getRowCount()) return;

                // Check for extra details
                if (currentSelectedObject.isComplexField(row)) {
                    if (currentSelectedObject.getComplexField(row) == null) {
                        say("This field can't be accessed!");
                        return;
                    }
                    DetailsWindow window = new DetailsWindow(controller, currentSelectedObject.getComplexField(row));
                    window.appear();
                }

                // Check for string details
                if (currentSelectedObject.isTextField(row)) {
                    TextWindow window = new TextWindow(controller, currentSelectedObject.getText(row));
                    window.appear();
                }
            }
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

    private void clear() {
        tableModel.clear();
        searchModel.clear();
        selectionComboBox.setSelectedIndex(-1);
    }
}
