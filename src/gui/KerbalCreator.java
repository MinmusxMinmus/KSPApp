package gui;

import controller.GUIController;
import kerbals.Job;

import javax.swing.*;

public class KerbalCreator extends KSPGUI {
    private JPanel mainPanel;
    private JLabel kerbalCreatorLabel;
    private JPanel dataPanel;
    private JPanel namePanel;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JCheckBox maleCheckBox;
    private JComboBox<Job> jobComboBox;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JCheckBox badassCheckBox;

    // Custom components

    public KerbalCreator(GUIController controller) {
        super(controller, KERBAL_CREATOR);
        setTitle("Kerbal creator");
        setContentPane(mainPanel);


    }
}
