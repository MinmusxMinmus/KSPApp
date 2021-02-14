package gui;

import controller.GUIController;
import other.KSPObject;
import other.display.KSPObjectTableModel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DetailsWindow extends KSPGUI{
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JTable detailsTable;

    private final KSPObjectTableModel tableModel;
    private final KSPObject object;

    public DetailsWindow(GUIController controller, KSPObject object) {
        super(controller, "Specific details");
        this.object = object;
        setContentPane(mainPanel);
        tableModel = new KSPObjectTableModel();
        tableModel.setItem(this.object);
        detailsTable.setModel(tableModel);

        detailsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int row = detailsTable.rowAtPoint(e.getPoint());
                if (row < 0 && row >= tableModel.getRowCount()) return;

                // Check for extra details
                if (object.isComplexField(row)) {
                    if (object.getComplexField(row) == null) {
                        say("This field can't be accessed!");
                        return;
                    }
                    DetailsWindow window = new DetailsWindow(controller, object.getComplexField(row));
                    window.appear("Specific details");
                }
            }
        });
    }
}
