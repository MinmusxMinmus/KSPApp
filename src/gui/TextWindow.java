package gui;

import controller.GUIController;

import javax.swing.*;

public class TextWindow extends KSPGUI{
    private JTextArea textArea;
    private JPanel mainPanel;

    public TextWindow(GUIController controller, String text) {
        super(controller, "Text");
        setContentPane(mainPanel);

        textArea.setText(text);
    }
}
