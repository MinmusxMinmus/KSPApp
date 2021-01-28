package gui;

import controller.GUIController;
import other.Editable;
import other.EditableKSPObjectTableModel;
import other.KSPObject;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EditWindow extends JDialog {

    private JPanel mainPanel;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JScrollPane tableScrollPane;
    private JTable dataTable;
    private JLabel missionCreatorLabel;

    private final EditableKSPObjectTableModel model;
    private final KSPGUI caller;

    public EditWindow(String title, Editable object, KSPGUI caller) {
        setTitle(title);
        this.caller = caller;
        this.model = new EditableKSPObjectTableModel(object);

        dataTable.setModel(model);
        model.setItem(object);

        listenerSetup();

    }

    private void listenerSetup() {
        // OK button
        OKButton.addActionListener(e -> {
            caller.setEdit(model.getItem());
            dispose();
        });

        // Cancel button
        cancelButton.addActionListener(e -> {
            dispose();
        });
    }
}
