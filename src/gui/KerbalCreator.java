package gui;

import controller.GUIController;
import kerbals.Job;
import other.util.KSPDate;

import javax.swing.*;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static java.lang.Integer.parseInt;

public class KerbalCreator extends KSPGUI {
    private JPanel mainPanel;
    private JLabel kerbalCreatorLabel;
    private JPanel dataPanel;
    private JPanel basicInformationPanel;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JTextField nameTextField;
    private JCheckBox maleCheckBox;
    private JCheckBox badassCheckBox;
    private JLabel nameLabel;
    private JComboBox<Job> jobComboBox;
    private JLabel jobLabel;
    private JLabel kermanLabel;
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
    private JCheckBox preciseTimeCheckBox;
    private JPanel descriptionPanel;
    private JTextArea descriptionTextArea;

    // Custom components
    private final DefaultComboBoxModel<Job> jobModel = new DefaultComboBoxModel<>();

    public KerbalCreator(GUIController controller) {
        super(controller, KERBAL_CREATOR);
        setContentPane(mainPanel);

        jobComboBox.setModel(jobModel);

        jobModel.addAll(Arrays.asList(Job.values()));

        listenerSetup();


    }

    private void listenerSetup() {
        // Precise time listener
        preciseTimeCheckBox.addItemListener(e -> {
            hourTextField.setEnabled(preciseTimeCheckBox.isSelected());
            minuteTextField.setEnabled(preciseTimeCheckBox.isSelected());
            secondTextField.setEnabled(preciseTimeCheckBox.isSelected());
            hourLabel.setEnabled(preciseTimeCheckBox.isSelected());
            minuteLabel.setEnabled(preciseTimeCheckBox.isSelected());
            secondLabel.setEnabled(preciseTimeCheckBox.isSelected());
        });

        // Cancel listener
        cancelButton.addActionListener(e -> {
            // Confirmation
            if (ask("Cancel kerbal creation", "Are you sure you want to cancel?")) {
                dispose();
            }
        });

        // OK listener
        OKButton.addActionListener(e -> {
            // Read values
            String name = nameTextField.getText().strip();
            boolean male = maleCheckBox.isSelected();
            boolean badass = badassCheckBox.isSelected();
            Job job = (Job) jobModel.getSelectedItem();
            String year = yearTextField.getText().strip();
            String day = dayTextField.getText().strip();
            String hour = hourTextField.getText().strip();
            String minute = minuteTextField.getText().strip();
            String second = secondTextField.getText().strip();
            String description = descriptionTextArea.getText().strip().equals("")
                    ? null
                    : descriptionTextArea.getText().strip();

            // Error checking
            if (name.equals("")
                    || year.equals("")
                    || day.equals("")
                    || (preciseTimeCheckBox.isSelected() && (hour.equals("") || minute.equals("") || second.equals("")))) {
                say("Please fill out all text fields!");
                return;
            }
            if (job == null) {
                say("Please select a job!");
                return;
            }

            // Confirmation
            if (!ask("Create kerbal", "Are you sure you want to create this kerbal?")) return;

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

            // Kerbal creation
            controller.createKerbalHired(name, male, badass, job, date);

            // Form end
            dispose();
        });
    }
}
