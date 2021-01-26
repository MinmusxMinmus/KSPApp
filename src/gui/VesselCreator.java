package gui;

import controller.GUIController;

import javax.swing.*;

public class VesselCreator extends KSPGUI {
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
    private JTextField partCountTextField;
    private JLabel partCountLabel;
    private JTextField textField1;
    private JPanel descriptionPanel;

    // Custom components

    public VesselCreator(GUIController controller) {
        super(controller, VESSEL_CREATOR);
        setContentPane(mainPanel);


    }
}
