package gui;

import controller.GUIController;
import other.util.Destination;
import other.display.GoodListModel;
import other.util.KSPDate;
import vessels.Concept;
import vessels.VesselProperty;
import vessels.VesselType;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;

import static java.lang.Integer.parseInt;

public class ConceptCreator extends KSPGUI {
    private JPanel mainPanel;
    private JLabel vesselCreatorLabel;
    private JPanel buttonsPanel;
    private JPanel dataPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel namePanel;
    private JLabel nameLabel;
    private JTextField nameTextField;
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
    private JPanel descriptionPanel;
    private JTextArea descriptionTextArea;
    private JCheckBox redesignCheckBox;
    private JComboBox<Concept> designComboBox;
    private JPanel locationsPanel;
    private JScrollPane locationsAvailableScrollPane;
    private JScrollPane locationsSelectedScrollPane;
    private JList<Destination> availableList;
    private JList<Destination> selectedList;
    private JLabel selectedLabel;
    private JLabel availableLabel;
    private JButton sortButton;
    private JComboBox<VesselType> typeComboBox;
    private JPanel propertiesPanel;
    private JLabel availablePropertiesLabel;
    private JScrollPane availablePropertiesScrollPane;
    private JList<VesselProperty> availablePropertiesList;
    private JScrollPane selectedPropertiesScrollPane;
    private JList<VesselProperty> selectedPropertiesList;
    private JLabel selectedPropertiesLabel;
    private JCheckBox preciseTimeCheckBox;

    // Custom components
    private final GoodListModel<Destination> availableModel = new GoodListModel<>(Destination.values());
    private final GoodListModel<Destination> selectedModel = new GoodListModel<>();

    private final GoodListModel<VesselProperty> availablePModel = new GoodListModel<>(VesselProperty.values());
    private final GoodListModel<VesselProperty> selectedPModel = new GoodListModel<>();

    private final DefaultComboBoxModel<Concept> designModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<VesselType> typeModel = new DefaultComboBoxModel<>();

    public ConceptCreator(GUIController controller) {
        super(controller, VESSEL_CREATOR);
        setContentPane(mainPanel);

        designComboBox.setModel(designModel);
        typeComboBox.setModel(typeModel);

        availableList.setModel(availableModel);
        selectedList.setModel(selectedModel);

        availablePropertiesList.setModel(availablePModel);
        selectedPropertiesList.setModel(selectedPModel);

        designModel.addAll(controller.getConcepts());
        typeModel.addAll(Arrays.asList(VesselType.values()));

        listenerSetup();

    }

    private void listenerSetup() {

        // Redesign checkbox
        redesignCheckBox.addActionListener(e -> {
            designComboBox.setEnabled(redesignCheckBox.isSelected());
            typeComboBox.setEnabled(!redesignCheckBox.isSelected());
        });

        // Precise time checkbox
        preciseTimeCheckBox.addItemListener(e -> {
            hourTextField.setEnabled(preciseTimeCheckBox.isSelected());
            minuteTextField.setEnabled(preciseTimeCheckBox.isSelected());
            secondTextField.setEnabled(preciseTimeCheckBox.isSelected());
            hourLabel.setEnabled(preciseTimeCheckBox.isSelected());
            minuteLabel.setEnabled(preciseTimeCheckBox.isSelected());
            secondLabel.setEnabled(preciseTimeCheckBox.isSelected());
        });

        // Available property listener
        availablePropertiesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = availablePropertiesList.locationToIndex(e.getPoint());
                if (index < 0 || index > availablePModel.getSize()) return;
                selectedPModel.add(availablePModel.pop(index));
            }
        });

        // Selected property listener
        selectedPropertiesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = selectedPropertiesList.locationToIndex(e.getPoint());
                if (index < 0 || index > selectedPModel.getSize()) return;
                availablePModel.add(selectedPModel.pop(index));
            }
        });

        // Available model listener
        availableList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = availableList.locationToIndex(e.getPoint());
                if (index < 0 || index > availableModel.getSize()) return;
                selectedModel.add(availableModel.pop(index));
            }
        });

        // Selected model listener
        selectedList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = selectedList.locationToIndex(e.getPoint());
                if (index < 0 || index > selectedModel.getSize() - 1) return;
                availableModel.add(selectedModel.pop(index));
            }
        });

        // Sort listener
        sortButton.addActionListener(e -> {
            selectedModel.sort(Comparator.naturalOrder());
            availableModel.sort(Comparator.naturalOrder());
        });

        // Cancel listener
        cancelButton.addActionListener(e -> {
            // Confirmation
            if (ask("Cancel vessel creation", "Are you sure you want to cancel?")) {
                dispose();
            }
        });

        // OK listener
        OKButton.addActionListener(e -> {
            // Read values
            String name = nameTextField.getText().strip();
            VesselType type = redesignCheckBox.isSelected()
                    ? null
                    : (VesselType) typeComboBox.getSelectedItem();
            Concept redesign = redesignCheckBox.isSelected()
                    ? (Concept) designComboBox.getSelectedItem()
                    : null;
            String year = yearTextField.getText().strip();
            String day = dayTextField.getText().strip();
            String hour = hourTextField.getText().strip();
            String minute = minuteTextField.getText().strip();
            String second = secondTextField.getText().strip();
            String description = descriptionTextArea.getText().strip().equals("")
                    ? null
                    : descriptionTextArea.getText().strip();
            VesselProperty[] properties = new VesselProperty[selectedPModel.getSize()];
            Destination[] destinations = new Destination[selectedModel.getSize()];
            for (int i = 0; i != selectedPModel.getSize(); i++) properties[i] = selectedPModel.getElementAt(i);
            for (int i = 0; i != selectedModel.getSize(); i++) destinations[i] = selectedModel.getElementAt(i);

            // Error checking
            if (name.equals("")
                    || year.equals("")
                    || day.equals("")
                    || (preciseTimeCheckBox.isSelected() && (hour.equals("") || minute.equals("") || second.equals("")))) {
                say("Please fill out all text fields!");
                return;
            }
            if (destinations.length == 0) {
                say("Please insert at least one destination!");
                return;
            }
            if ((!redesignCheckBox.isSelected() && type == null) || (redesignCheckBox.isSelected() && redesign == null)) {
                say("Please choose either a vessel type, or the inspiration concept.");
                return;
            }

            // Confirmation
            if (!ask("Create vessel", "Are you sure you want to create this vessel concept?")) return;

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

            // Creating vessel
            controller.createConcept(name, type, redesign, date, destinations, properties);

            // Form end
            dispose();
        });

    }
}
