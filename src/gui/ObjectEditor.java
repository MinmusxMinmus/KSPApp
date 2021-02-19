package gui;

import controller.GUIController;
import other.KSPObject;
import other.display.EditableKSPObjectTableModel;

import javax.swing.*;

public class ObjectEditor extends KSPGUI {
    private JPanel mainPanel;
    private JPanel buttonsPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel tablePanel;
    private JTable table;
    private JTextArea textArea;

    private final KSPObject object;
    private final EditableKSPObjectTableModel tableModel;


    public ObjectEditor(GUIController controller, KSPObject object) {
        super(controller, "Edit object");
        setContentPane(mainPanel);
        this.object = object;
        this.tableModel = new EditableKSPObjectTableModel(object);

        textArea.setText(object.getDescription());
        table.setModel(tableModel);

        listenerSetup();
    }

    private void listenerSetup() {
        okButton.addActionListener(e -> {
            if (!ask("Save changes", "Are you sure you want to apply the changes?")) return;

            // Change fields
            object.setDescription(textArea.getText().strip());

            controller.ready();
            dispose();
        });

        cancelButton.addActionListener(e -> {
            dispose();
        });
    }
}
