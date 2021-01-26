package gui;

import controller.GUIController;

import javax.swing.*;
import java.awt.*;

public abstract class KSPGUI extends JFrame {

    protected static final String KERBAL_CREATOR = "Kerbal creator";
    protected static final String MISSION_CREATOR = "Mission creator";
    protected static final String VESSEL_CREATOR = "Vessel creator";
    protected static final String MAIN_SCREEN = "KSP Autism App";

    protected final GUIController controller;

    public KSPGUI(GUIController controller, String title) {
        this.controller = controller;
        try { UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); }
        catch (Exception e) { e.printStackTrace(); }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(title);
    }

    public void appear(String title) {
        pack();
        setVisible(true);
        setTitle(title);
    }

    /** Utility method to display a choice box with the desired message.
     * @param title The window's name.
     * @param message The message displayed.
     * @return true if the user clicked on "Yes", false otherwise.
     */
    protected boolean ask(String title, String message) {
        int ret = JOptionPane.showOptionDialog(getContentPane(),
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null);
        return ret == JOptionPane.YES_OPTION;
    }

    protected void say(String message) {
        JOptionPane.showMessageDialog(getContentPane(), message);
    }
}
