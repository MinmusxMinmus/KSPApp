package gui;

import controller.GUIController;
import other.interfaces.Editable;

import javax.swing.*;

public abstract class KSPGUI extends JFrame {

    protected static final String KERBAL_CREATOR = "Kerbal creator";
    protected static final String MISSION_CREATOR = "Mission creator";
    protected static final String VESSEL_CREATOR = "Vessel creator";
    protected static final String MAIN_SCREEN = "KSP Autism App";

    protected final GUIController controller;
    protected Editable edit;

    public KSPGUI(GUIController controller, String title) {
        this.controller = controller;
        try { UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); }
        catch (Exception e) { e.printStackTrace(); }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(title);
    }

    public void appear() {
        pack();
        setVisible(true);
    }

    /** Utility method to display a choice box with the desired message.
     * @param title The window's name.
     * @param message The message displayed.
     * @return true if the user clicked on "Yes", false otherwise.
     */
    protected boolean ask(String title, String message) {
        return JOptionPane.showOptionDialog(getContentPane(),
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null) == JOptionPane.YES_OPTION;
    }

    protected void say(String message) {
        JOptionPane.showMessageDialog(getContentPane(), message);
    }

    protected String askString(String title, String message) {
        return JOptionPane.showInputDialog(getContentPane(),
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void setEdit(Editable edit) {
        this.edit = edit;
    }
}
