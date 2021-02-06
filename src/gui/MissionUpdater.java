package gui;

import controller.GUIController;
import missions.Mission;
import other.util.KSPDate;

import javax.swing.*;
import java.awt.*;
import java.time.OffsetDateTime;

import static java.lang.Integer.parseInt;

public class MissionUpdater extends KSPGUI {

    private static final String LOG_PANEL = "Log";
    private static final String VESSEL_PANEL = "Vessels";
    private static final String CREW_PANEL = "Crew";
    private static final String RESCUE_PANEL = "Rescue";
    private static final String UPDATE_VESSEL_PANEL = "UpdateVessels";
    private static final String UPDATE_CREW_PANEL = "UpdateCrew";
    private static final String MOVE_CREW_PANEL = "MoveCrew";
    private static final String CONDECORATION_PANEL = "Condecoration";

    private JPanel mainPanel;
    private JPanel cardPanel;
    private JPanel choosingPanel;
    private JLabel missionCreatorLabel;
    private JPanel choosingButtonsPanel;
    private JButton newLogEntryButton;
    private JButton addRemoveCrewButton;
    private JButton updateCrewButton;
    private JButton addRemoveVesselsButton;
    private JButton newRescuedKerbalButton;
    private JButton updateVesselButton;
    private JPanel logPanel;
    private JLabel missionLogsLabel;
    private JPanel namePanel;
    private JLabel celestialBodyLabel;
    private JComboBox celestialBodyComboBox;
    private JCheckBox inSpaceCheckBox;
    private JPanel briefingPanel;
    private JTextArea briefingTextArea;
    private JPanel vesselPanel;
    private JLabel vesselManagerLabel;
    private JPanel crewPanel;
    private JLabel crewManagerLabel;
    private JPanel rescuePanel;
    private JLabel newRescuedKerbalLabel;
    private JPanel updateVesselPanel;
    private JLabel statusUpdateLabel;
    private JPanel updateCrewPanel;
    private JLabel newCondecorationLabel;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel vesselTablesPanel;
    private JScrollPane vesselFreePane;
    private JScrollPane vesselAssignedPane;
    private JTable table1;
    private JTable table2;
    private JPanel crewTablesPanel;
    private JScrollPane crewFreePane;
    private JScrollPane crewAssignedPane;
    private JTable crewFreeTable;
    private JTable crewAssignedTable;
    private JPanel basicInformationPanel;
    private JCheckBox maleCheckBox;
    private JCheckBox badassCheckBox;
    private JComboBox jobComboBox;
    private JLabel jobLabel;
    private JTextField nameTextField;
    private JLabel nameLabel;
    private JLabel kermanLabel;
    private JPanel rescuedDetailsPanel;
    private JPanel rescuedNamePanel;
    private JComboBox rescueVesselComboBox;
    private JPanel rescueVesselPanel;
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
    private JPanel somethingPanel;
    private JComboBox updateVesselComboBox;
    private JComboBox updateStatusComboBox;
    private JLabel updateStatusLabel;
    private JComboBox celestialBodyComboBox1;
    private JCheckBox inSpaceCheckBox1;
    private JPanel vesselInformationPanel;
    private JLabel celestialBodyLabel1;
    private JCheckBox KIACheckBox;
    private JLabel detailsLabel;
    private JTextField detailsTextField;
    private JButton condecorationsButton;
    private JPanel crewChooserPanel;
    private JPanel crewLocationPanel;
    private JPanel crewDeathPanel;
    private JButton moveCrewAroundButton;
    private JPanel moveCrewPanel;
    private JPanel condecorationPanel;
    private JLabel crewMoverLabel;
    private JLabel condecorationCenterLabel;
    private JComboBox condecorationCrewComboBox;
    private JPanel condecorationCrewPanel;
    private JTextArea condecorationTextArea;
    private JPanel condecorationAwardPanel;
    private JPanel crewMovePanel;
    private JComboBox crewMoveComboBox;
    private JTable newVesselTable;
    private JScrollPane newVesselPane;

    private CardLayout cardLayout;
    private final Mission mission;
    private String currentCard = null;


    public MissionUpdater(GUIController controller, String title, Mission m) {
        super(controller, title);
        this.mission = m;
        setContentPane(mainPanel);
        cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(mainPanel, "Choices");

        // Default date: last event's date
        KSPDate defaultDate = m.getEvents().get(m.getEvents().size()).getDate();
        if (defaultDate != null) {
            yearTextField.setText(Integer.toString(defaultDate.getYear()));
            dayTextField.setText(Integer.toString(defaultDate.getDay()));
            hourTextField.setText(Integer.toString(defaultDate.getHour()));
            minuteTextField.setText(Integer.toString(defaultDate.getMinute()));
            secondTextField.setText(Integer.toString(defaultDate.getSecond()));
        }

        listenerSetup();
    }

    private void listenerSetup() {

        // Different card button listeners
        newLogEntryButton.addActionListener(e -> {
            cardLayout.show(mainPanel, LOG_PANEL);
            currentCard = LOG_PANEL;
        });
        addRemoveCrewButton.addActionListener(e -> {
            cardLayout.show(mainPanel, CREW_PANEL);
            currentCard = CREW_PANEL;
        });
        updateCrewButton.addActionListener(e -> {
            cardLayout.show(mainPanel, UPDATE_CREW_PANEL);
            currentCard = UPDATE_CREW_PANEL;
        });
        addRemoveVesselsButton.addActionListener(e -> {
            cardLayout.show(mainPanel, VESSEL_PANEL);
            currentCard = VESSEL_PANEL;
        });
        newRescuedKerbalButton.addActionListener(e -> {
            cardLayout.show(mainPanel, RESCUE_PANEL);
            currentCard = RESCUE_PANEL;
        });
        updateVesselButton.addActionListener(e -> {
            cardLayout.show(mainPanel, UPDATE_VESSEL_PANEL);
            currentCard = UPDATE_VESSEL_PANEL;
        });
        condecorationsButton.addActionListener(e -> {
            cardLayout.show(mainPanel, CONDECORATION_PANEL);
            currentCard = CONDECORATION_PANEL;
        });
        moveCrewAroundButton.addActionListener(e -> {
            cardLayout.show(mainPanel, MOVE_CREW_PANEL);
            currentCard = MOVE_CREW_PANEL;
        });

        // Cancel listener
        cancelButton.addActionListener(e -> {
            // Confirmation
            if (ask("Cancel mission editing", "Are you sure you want to cancel?")) {
                dispose();
            }
        });

        // OK listener
        OKButton.addActionListener(e -> {

            // Date values
            String year = yearTextField.getText();
            String day = dayTextField.getText();
            String hour = hourTextField.getText();
            String minute = minuteTextField.getText();
            String second = secondTextField.getText();

            // Date error checking
            if (year.strip().equals("")
                    || day.strip().equals("")
                    || preciseTimeCheckBox.isSelected() && (
                    hour.strip().equals("")
                            || minute.strip().equals("")
                            || second.strip().equals(""))) {
                say("Please fill out all date fields!");
                return;
            }

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

            switch (currentCard) {
                // TODO implement specific confirmation checks and messages
                default -> {
                    boolean confirm = ask("No edits", "No changes will be applied. Are you sure?");
                    if (confirm) dispose();
                }
            }

            // Form end
            dispose();
        });

        logPanelListeners();
        vesselPanelListeners();
        crewPanelListeners();
        rescuePanelListeners();
        updateVesselListeners();
        updateCrewListeners();
        moveCrewListeners();
        condecorationListeners();
    }

    private void condecorationListeners() {
        // TODO
    }

    private void moveCrewListeners() {
        // TODO
    }

    private void updateCrewListeners() {
        // TODO
    }

    private void updateVesselListeners() {
        // TODO
    }

    private void rescuePanelListeners() {
        // TODO
    }

    private void crewPanelListeners() {
        // TODO
    }

    private void vesselPanelListeners() {
        // TODO
    }

    private void logPanelListeners() {
        // TODO
    }


    // TODO Crew updater: only show crew that's not inside a ship
}
