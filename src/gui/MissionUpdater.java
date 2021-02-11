package gui;

import controller.GUIController;
import kerbals.Job;
import kerbals.Kerbal;
import missions.Mission;
import other.display.GoodListModel;
import other.display.MissionAssignedKerbalTableModel;
import other.display.MissionKerbalTableModel;
import other.util.CelestialBody;
import other.util.KSPDate;
import other.util.Location;
import vessels.Vessel;
import vessels.VesselStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class MissionUpdater extends KSPGUI {

    private static final String CHOICES_PANEL = "Choices";
    private static final String LOG_PANEL = "Log";
    private static final String VESSEL_PANEL = "Vessels";
    private static final String CREW_PANEL = "Crew";
    private static final String RESCUE_PANEL = "Rescue";
    private static final String UPDATE_VESSEL_PANEL = "UpdateVessels";
    private static final String UPDATE_CREW_PANEL = "UpdateCrew";
    private static final String MOVE_CREW_PANEL = "MoveCrew";
    private static final String CONDECORATION_PANEL = "Condecoration";

    private static final String DEFAULT_RESCUED_POSITION = "Rescued member";

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
    private JComboBox<CelestialBody> celestialBodyComboBox;
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
    private JLabel crewUpdateLabel;
    private JPanel buttonsPanel;
    private JButton OKButton;
    private JButton cancelButton;
    private JPanel vesselTablesPanel;
    private JScrollPane vesselFreePane;
    private JScrollPane vesselAssignedPane;
    private JPanel crewTablesPanel;
    private JScrollPane crewFreePane;
    private JScrollPane crewAssignedPane;
    private JTable crewFreeTable;
    private JTable crewAssignedTable;
    private JPanel basicInformationPanel;
    private JCheckBox maleCheckBox;
    private JCheckBox badassCheckBox;
    private JComboBox<Job> jobComboBox;
    private JLabel jobLabel;
    private JTextField nameTextField;
    private JLabel nameLabel;
    private JLabel kermanLabel;
    private JPanel rescuedDetailsPanel;
    private JPanel rescuedNamePanel;
    private JComboBox<Vessel> rescueVesselComboBox;
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
    private JComboBox<Vessel> updateVesselComboBox;
    private JComboBox<VesselStatus> updateStatusComboBox;
    private JLabel updateStatusLabel;
    private JComboBox<CelestialBody> updateVesselBodyComboBox;
    private JCheckBox updateVesselInSpaceCheckBox;
    private JPanel vesselInformationPanel;
    private JLabel updateVesselBodyLabel;
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
    private JComboBox<Kerbal> condecorationCrewComboBox;
    private JPanel condecorationCrewPanel;
    private JTextArea condecorationTextArea;
    private JPanel condecorationAwardPanel;
    private JPanel crewMovePanel;
    private JComboBox<Kerbal> crewMoveComboBox;
    private JScrollPane newVesselPane;
    private JList<Vessel> newVesselList;
    private JComboBox<Kerbal> updateCrewComboBox;
    private JComboBox<CelestialBody> updateCrewBodyComboBox;
    private JCheckBox updateCrewInSpaceCheckBox;
    private JLabel celestialBodyLabel2;
    private JTextField missionPositionTextField;
    private JLabel missionPositionLabel;
    private JList<Vessel> availableVesselsList;
    private JList<Vessel> assignedVesselsList;
    private JPanel updateVesselDetailsPanel;
    private JTextField updateVesselDetailsTextField;
    private JPanel moveCrewDetailsPanel;
    private JTextField moveCrewDetailsTextField;
    private JPanel rescueDetailsPanel;
    private JTextField rescueDetailsTextField;
    private JPanel crewDetailsPanel;
    private JTextField crewDetailsTextField;
    private JPanel vesselDetailsPanel;
    private JTextField vesselDetailsTextField;
    private JPanel condecorationTitlePanel;
    private JTextField condecorationTitleTextField;
    private JButton endMissionButton;
    private JPanel missionEndPanel;
    private JTextArea missionEndTextArea;

    private final CardLayout cardLayout;
    private final Mission mission;
    private String currentCard;


    public MissionUpdater(GUIController controller, String title, Mission m) {
        super(controller, title);
        this.mission = m;
        setContentPane(mainPanel);
        cardLayout = (CardLayout) cardPanel.getLayout();

        cardLayout.show(cardPanel, CHOICES_PANEL);
        currentCard = CHOICES_PANEL;

        // Default date: last event's date, or mission start
        KSPDate defaultDate;
        if (m.getEvents().size() != 0) defaultDate = m.getEvents().get(m.getEvents().size() - 1).getDate();
        else defaultDate = m.getStart();

        yearTextField.setText(Integer.toString(defaultDate.getYear()));
        dayTextField.setText(Integer.toString(defaultDate.getDay()));
        hourTextField.setText(Integer.toString(defaultDate.getHour()));
        minuteTextField.setText(Integer.toString(defaultDate.getMinute()));
        secondTextField.setText(Integer.toString(defaultDate.getSecond()));

        logPanelSetup();
        vesselPanelSetup();
        crewPanelSetup();
        rescuePanelSetup();
        updateVesselSetup();
        updateCrewSetup();
        moveCrewSetup();
        condecorationSetup();
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

        // Different card button listeners
        newLogEntryButton.addActionListener(e -> {
            cardLayout.show(cardPanel, LOG_PANEL);
            currentCard = LOG_PANEL;
        });
        addRemoveCrewButton.addActionListener(e -> {
            cardLayout.show(cardPanel, CREW_PANEL);
            currentCard = CREW_PANEL;
        });
        updateCrewButton.addActionListener(e -> {
            cardLayout.show(cardPanel, UPDATE_CREW_PANEL);
            currentCard = UPDATE_CREW_PANEL;
        });
        addRemoveVesselsButton.addActionListener(e -> {
            cardLayout.show(cardPanel, VESSEL_PANEL);
            currentCard = VESSEL_PANEL;
        });
        newRescuedKerbalButton.addActionListener(e -> {
            cardLayout.show(cardPanel, RESCUE_PANEL);
            currentCard = RESCUE_PANEL;
        });
        updateVesselButton.addActionListener(e -> {
            cardLayout.show(cardPanel, UPDATE_VESSEL_PANEL);
            currentCard = UPDATE_VESSEL_PANEL;
        });
        condecorationsButton.addActionListener(e -> {
            cardLayout.show(cardPanel, CONDECORATION_PANEL);
            currentCard = CONDECORATION_PANEL;
        });
        moveCrewAroundButton.addActionListener(e -> {
            cardLayout.show(cardPanel, MOVE_CREW_PANEL);
            currentCard = MOVE_CREW_PANEL;
        });

        // Mission end listener
        endMissionButton.addActionListener(e -> {
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

            // Get details
            String details = missionEndTextArea.getText().strip();
            if (details.equals("")) {
                say("Please add details regarding the mission end!");
                return;
            }

            if (!ask("End mission", "Are you sure you want to end this mission? You won't be able to edit it anymore.")) return;

            mission.missionEnd(details, date);

            say("Succesfully ended mission");
            dispose();
        });

        // Cancel listener
        cancelButton.addActionListener(e -> {
            // Confirmation
            if (currentCard.equals(CHOICES_PANEL)) dispose();
            else if (ask("Cancel mission editing", "Are you sure you want to cancel?")) {
                cardLayout.show(cardPanel, CHOICES_PANEL);
                currentCard = CHOICES_PANEL;
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
                case CHOICES_PANEL -> dispose();

                case LOG_PANEL -> {
                    // Get celestial body
                    CelestialBody body = (CelestialBody) celestialBodyComboBox.getSelectedItem();
                    if (body == null) {
                        say("Please select a celestial body!");
                        return;
                    }

                    // Get in space
                    boolean inSpace = inSpaceCheckBox.isSelected();

                    // Get briefing
                    String briefing = briefingTextArea.getText().strip();
                    if (briefing.equals("")) {
                        say("Please add some event details!");
                        return;
                    }

                    if (!ask("Add event", "Are you sure you want to add this event to the mission logs?")) return;

                    mission.logEvent(new Location(inSpace, body), date, briefing);
                }

                case VESSEL_PANEL -> {
                    // Get vessels to add
                    List<Vessel> newMissionVessels = ((GoodListModel<Vessel>)assignedVesselsList.getModel()).getItems();

                    // Get vessels to remove
                    List<Vessel> newNotInMissionVessels = ((GoodListModel<Vessel>)availableVesselsList.getModel()).getItems();

                    // Get details
                    String details = vesselDetailsTextField.getText().strip();
                    if (details.equals("")) {
                        say("Please add some details regarding the added/deleted vessels!");
                        return;
                    }

                    if (!ask("Change vessels", "Are you sure you want to change the vessels as indicated?")) return;

                    mission.updateVessels(newNotInMissionVessels, newMissionVessels, date, details);
                }

                case CREW_PANEL -> {
                    // Get crew to remove
                    Set<Kerbal> newNotInMissionCrew = ((MissionKerbalTableModel)crewFreeTable.getModel()).getCrew();

                    // Get crew to add
                    Map<Kerbal, String> newMissionCrew = ((MissionAssignedKerbalTableModel)crewAssignedTable.getModel()).getCrew2();

                    // Get details
                    String details = crewDetailsTextField.getText().strip();
                    if (details.equals("")) {
                        say("Please add some details regarding the added/deleted crew members!");
                        return;
                    }

                    if (!ask("Change crew", "Are you sure you want to change the crew as indicated?")) return;

                    mission.updateCrew(newNotInMissionCrew, newMissionCrew, date, details);
                }

                case RESCUE_PANEL -> {
                    // Get name
                    String name = nameTextField.getText().strip();
                    if (name.equals("")) {
                        say("Please insert the kerbal's name!");
                        return;
                    }

                    // Get male
                    boolean male = maleCheckBox.isSelected();

                    // Get badass
                    boolean badass = badassCheckBox.isSelected();

                    // Get job
                    Job job = (Job) jobComboBox.getSelectedItem();
                    if (job == null) {
                        say("Please select the kerbal's job!");
                        return;
                    }

                    // Get rescuer
                    Vessel rescuer = (Vessel) rescueVesselComboBox.getSelectedItem();
                    if (rescuer == null) {
                        say("Please select the rescuing vessel!");
                        return;
                    }

                    // Get mission position
                    String position = missionPositionTextField.getText().strip();
                    if (position.equals("")) {
                        say("Please insert the kerbal's position on the mission!");
                        return;
                    }

                    // Get details
                    String details = rescueDetailsTextField.getText().strip();
                    if (details.equals("")) {
                        say("Please add some details regarding the kerbal's rescue!");
                        return;
                    }

                    if (!ask("Rescue kerbal", "Are you sure you want to rescue this kerbal?")) return;

                    mission.kerbalRescued(name, male, badass, job, date, rescuer, position, details);
                }

                case UPDATE_VESSEL_PANEL -> {
                    // Get vessel
                    Vessel v = (Vessel) updateVesselComboBox.getSelectedItem();
                    if (v == null) {
                        say("Please select a vessel!");
                        return;
                    }

                    // Get status
                    VesselStatus status = (VesselStatus) updateStatusComboBox.getSelectedItem();
                    if (status == null) {
                        say("Please select a status!");
                        return;
                    }

                    // Get celestial body
                    CelestialBody body = (CelestialBody) updateVesselBodyComboBox.getSelectedItem();
                    if (body == null) {
                        say("Please select a celestial body!");
                        return;
                    }

                    // Get in space
                    boolean inSpace = updateVesselInSpaceCheckBox.isSelected();

                    // Get details
                    String details = updateVesselDetailsTextField.getText().strip();
                    if (details.equals("")) {
                        say("Please add some details regarding the vessel update!");
                        return;
                    }

                    if (!ask("Update vessel", "Are you sure you want to update the vessel?")) return;


                    mission.updateVessel(v, status, new Location(inSpace, body), date, details);
                }

                case UPDATE_CREW_PANEL -> {
                    // Get kerbal
                    Kerbal k = (Kerbal) updateCrewComboBox.getSelectedItem();
                    if (k == null) {
                        say("Please select a crew member!");
                        return;
                    }

                    // Get celestial body
                    CelestialBody body = (CelestialBody) updateCrewBodyComboBox.getSelectedItem();
                    if (body == null) {
                        say("Please select a celestial body!");
                        return;
                    }

                    // Get in space
                    boolean inSpace = updateCrewInSpaceCheckBox.isSelected();

                    // Get details
                    String details = detailsTextField.getText().strip();
                    if (details.equals("")) {
                        say("Please add some details regarding the crew update!");
                        return;
                    }

                    if (!ask("Update crew", "Are you sure you want to update this crew member?")) return;

                    // Location
                    Location location = new Location(inSpace, body);

                    // Apply changes
                    if (KIACheckBox.isSelected()) mission.KIA(k, date, details);
                    else mission.moveCrew(k, location, date, details);
                }

                case MOVE_CREW_PANEL -> {
                    // Get kerbal
                    Kerbal k = (Kerbal) crewMoveComboBox.getSelectedItem();
                    if (k == null) {
                        say("Please select a crew member!");
                        return;
                    }

                    // Get vessels
                    Vessel newVessel = newVesselList.getSelectedValue();
                    Vessel currentVessel = controller.getInstance(k.getVessel());

                    // Get details
                    String details = moveCrewDetailsTextField.getText().strip();
                    if (details.equals("")) {
                        say("Please add some details regarding the transfer!");
                        return;
                    }

                    // Case 1: no selected vessel
                    if (newVessel == null) {
                        // Case 1.1: no current vessel as well
                        if (currentVessel == null) {
                            say("This member doesn't seem to be in a vessel. Select a vessel to join, or try with a different member!");
                            return;
                        }
                        // Case 1.2: leaving the current vessel
                        if (!ask("Exit vessel", k.getName() + " Kerman will leave \"" + currentVessel.getName() + "\". Are you sure?")) return;
                        mission.leftVessel(k, currentVessel, date, details);
                    }
                    // Case 2: selected vessel
                    else {
                        // Case 2.1: kerbal inside a vessel
                        if (currentVessel != null) {
                            say("This crew member is already in a vessel. Make it leave the current vessel first (select nothing), then try again!");
                            return;
                        }
                        // Case 2.2: entering selected vessel
                        if (!ask("Enter vessel", k.getName() + " Kerman will board \"" + newVessel.getName() + "\". Are you sure?")) return;
                        mission.enteredVessel(k, newVessel, date, details);
                    }
                }

                case CONDECORATION_PANEL -> {
                    // Get kerbal
                    Kerbal k = (Kerbal) condecorationCrewComboBox.getSelectedItem();
                    if (k == null) {
                        say("Please select a crew member!");
                        return;
                    }

                    // Get title
                    String title = condecorationTitleTextField.getText().strip();
                    if (title.equals("")) {
                        say("Please add a title!");
                        return;
                    }

                    // Get condecoration
                    String condecorationText = condecorationTextArea.getText().strip();
                    if (condecorationText == null || condecorationText.equals("")) {
                        say("Please add a condecoration!");
                        return;
                    }

                    if (!ask("Award condecoration", "Are you sure you want to award this condecoration?")) return;

                    // Award condecoration
                    mission.awardCondecoration(k, title, condecorationText, date);
                }

                default -> {
                    boolean confirm = ask("What window are you even at?", "No changes will be applied. Are you sure?");
                    if (!confirm) return;
                }
            }

            // Back to beginning
            cardLayout.show(cardPanel, CHOICES_PANEL);
            currentCard = CHOICES_PANEL;

            // Update every window again, since things might have changed.
            logPanelSetup();
            vesselPanelSetup();
            crewPanelSetup();
            rescuePanelSetup();
            updateVesselSetup();
            updateCrewSetup();
            moveCrewSetup();
            condecorationSetup();
        });
    }

    private void condecorationSetup() {
        // Kerbal combo box
        DefaultComboBoxModel<Kerbal> crewModel = new DefaultComboBoxModel<>();
        condecorationCrewComboBox.setModel(crewModel);
        crewModel.addAll(mission.getCrew());

        // No dynamic changes, no listeners
    }

    private void moveCrewSetup() {
        // Kerbal combo box
        DefaultComboBoxModel<Kerbal> crewModel = new DefaultComboBoxModel<>();
        crewMoveComboBox.setModel(crewModel);
        crewModel.addAll(mission.getCrew());

        // Vessel list
        GoodListModel<Vessel> listModel = new GoodListModel<>();
        newVesselList.setModel(listModel);

        // Crew listener
        crewMoveComboBox.addActionListener(e -> {
            Kerbal k = (Kerbal) crewModel.getSelectedItem();
            if (k == null) return; // Shouldn't happen, but who knows?
            Location location = k.getLocation();
            boolean inShip = k.getVessel() != 0;
            Vessel v = controller.getInstance(k.getVessel());

            listModel.clear(); // Remove all previous vessels

            // Nearby vessels
            Set<Vessel> nearbyVessels = controller.getVessels().stream()
                    .filter(ve -> ve.getLocation().closeTo(location))
                    .filter(ve -> !ve.getStatus().equals(VesselStatus.CRASHED))
                    .collect(Collectors.toSet());

            if (inShip) { // If the kerbal is in a ship, add connected vessels first
                Set<Vessel> vessels = v.getVessels();
                vessels.forEach(ve -> {
                    nearbyVessels.remove(ve); // Remove duplicates
                    listModel.add(ve);
                });
            }
            // Afterwards, add all vessels that are close enough
            nearbyVessels.forEach(listModel::add);
        });
    }

    private void updateCrewSetup() {
        // Kerbal combo box
        DefaultComboBoxModel<Kerbal> crewModel = new DefaultComboBoxModel<>();
        updateCrewComboBox.setModel(crewModel);
        Set<Kerbal> EVAKerbals = mission.getCrew().stream()
                .filter(k -> k.getVessel() == 0)
                .collect(Collectors.toSet());
        crewModel.addAll(EVAKerbals);

        // Celestial body combo box
        DefaultComboBoxModel<CelestialBody> bodyModel = new DefaultComboBoxModel<>();
        updateCrewBodyComboBox.setModel(bodyModel);
        bodyModel.addAll(Arrays.asList(CelestialBody.values()));

        // Death listener
        KIACheckBox.addActionListener(e -> {
            detailsLabel.setEnabled(KIACheckBox.isSelected());
            detailsTextField.setEnabled(KIACheckBox.isSelected());
        });
    }

    private void updateVesselSetup() {
        // Vessel combo box
        DefaultComboBoxModel<Vessel> vesselModel = new DefaultComboBoxModel<>();
        updateVesselComboBox.setModel(vesselModel);
        vesselModel.addAll(mission.getVessels(VesselStatus.NOMINAL));
        vesselModel.addAll(mission.getVessels(VesselStatus.STRANDED));

        // Status combo box
        DefaultComboBoxModel<VesselStatus> statusModel = new DefaultComboBoxModel<>();
        updateStatusComboBox.setModel(statusModel);
        statusModel.addAll(Arrays.asList(VesselStatus.values()));

        // Celestial body combo box
        DefaultComboBoxModel<CelestialBody> bodyModel = new DefaultComboBoxModel<>();
        updateVesselBodyComboBox.setModel(bodyModel);
        bodyModel.addAll(Arrays.asList(CelestialBody.values()));

        // Vessel listener
        updateVesselComboBox.addActionListener(e -> {
            Vessel v = (Vessel) vesselModel.getSelectedItem();
            if (v == null) return; // Who knows, again?
            // Match values
            statusModel.setSelectedItem(v.getStatus());
            bodyModel.setSelectedItem(v.getLocation().getCelestialBody());
            inSpaceCheckBox.setSelected(v.getLocation().isInSpace());
        });

    }

    private void rescuePanelSetup() {
        // Vessel combo box
        DefaultComboBoxModel<Vessel> vesselModel = new DefaultComboBoxModel<>();
        rescueVesselComboBox.setModel(vesselModel);
        vesselModel.addAll(mission.getVessels(VesselStatus.NOMINAL));
        vesselModel.addAll(mission.getVessels(VesselStatus.STRANDED));

        // Default mission position
        missionPositionTextField.setText(DEFAULT_RESCUED_POSITION);

        // Job combo box
        DefaultComboBoxModel<Job> jobModel = new DefaultComboBoxModel<>();
        jobComboBox.setModel(jobModel);
        jobModel.addAll(Arrays.asList(Job.values()));

        // No dynamic updating, no listeners
    }

    private void crewPanelSetup() {
        // Crew tables
        Set<Kerbal> crewNotInMission = controller.getKerbals().stream()
                .filter(k -> !k.getMissions().contains(mission))
                .collect(Collectors.toSet());
        Set<Kerbal> crewInMission = mission.getCrew();
        MissionKerbalTableModel availableModel = new MissionKerbalTableModel(crewNotInMission);
        MissionAssignedKerbalTableModel assignedModel = new MissionAssignedKerbalTableModel(crewInMission);
        crewFreeTable.setModel(availableModel);
        crewAssignedTable.setModel(assignedModel);

        // Available kerbal table listener
        crewFreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = crewFreeTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < availableModel.getRowCount()) {
                    Kerbal k = availableModel.getKerbal(row);
                    availableModel.removeKerbal(k);
                    assignedModel.addKerbal(k);
                }
                // Update date fields
                KSPDate last;
                if (mission.getEvents().isEmpty()) last = new KSPDate(controller, 0, 0, 0, 0, 0);
                else last = mission.getEvents().get(mission.getEvents().size()).getDate();
                yearTextField.setText(Integer.toString(last.getYear()));
                dayTextField.setText(Integer.toString(last.getDay()));
                hourTextField.setText(Integer.toString(last.getHour()));
                minuteTextField.setText(Integer.toString(last.getMinute()));
                secondTextField.setText(Integer.toString(last.getSecond()));

                assignedModel.getCrew().stream().map(Kerbal::getHiringDate).forEach(hiringDate -> {
                    if (hiringDate.after(
                            Integer.parseInt(yearTextField.getText()),
                            Integer.parseInt(dayTextField.getText()),
                            Integer.parseInt(hourTextField.getText()),
                            Integer.parseInt(minuteTextField.getText()),
                            Integer.parseInt(secondTextField.getText())
                    )) {
                        yearTextField.setText(Integer.toString(hiringDate.getYear()));
                        dayTextField.setText(Integer.toString(hiringDate.getDay()));
                        hourTextField.setText(Integer.toString(hiringDate.getHour()));
                        minuteTextField.setText(Integer.toString(hiringDate.getMinute()));
                        secondTextField.setText(Integer.toString(hiringDate.getSecond()));
                    }
                });
            }
        });

        // Selected kerbal table listener
        crewAssignedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = crewAssignedTable.rowAtPoint(e.getPoint());
                int col = crewAssignedTable.columnAtPoint(e.getPoint());
                if (row >= 0)
                    if (row < assignedModel.getRowCount() // Is a row on the table
                            && col != assignedModel.getColumnCount() - 1 // Is not the last cell (Position in the mission)
                            && crewAssignedTable.getSelectedRow() == row) { // Double click
                        Kerbal k = assignedModel.getKerbal(row);
                        assignedModel.removeKerbal(k);
                        availableModel.addKerbal(k);
                    }
                // Update date fields
                KSPDate last;
                if (mission.getEvents().isEmpty()) last = new KSPDate(controller, 0, 0, 0, 0, 0);
                else last = mission.getEvents().get(mission.getEvents().size()).getDate();
                yearTextField.setText(Integer.toString(last.getYear()));
                dayTextField.setText(Integer.toString(last.getDay()));
                hourTextField.setText(Integer.toString(last.getHour()));
                minuteTextField.setText(Integer.toString(last.getMinute()));
                secondTextField.setText(Integer.toString(last.getSecond()));

                assignedModel.getCrew().stream().map(Kerbal::getHiringDate).forEach(hiringDate -> {
                    if (hiringDate.after(
                            Integer.parseInt(yearTextField.getText()),
                            Integer.parseInt(dayTextField.getText()),
                            Integer.parseInt(hourTextField.getText()),
                            Integer.parseInt(minuteTextField.getText()),
                            Integer.parseInt(secondTextField.getText())
                    )) {
                        yearTextField.setText(Integer.toString(hiringDate.getYear()));
                        dayTextField.setText(Integer.toString(hiringDate.getDay()));
                        hourTextField.setText(Integer.toString(hiringDate.getHour()));
                        minuteTextField.setText(Integer.toString(hiringDate.getMinute()));
                        secondTextField.setText(Integer.toString(hiringDate.getSecond()));
                    }
                });
            }
        });
    }

    private void vesselPanelSetup() {
        // Vessel lists
        GoodListModel<Vessel> availableModel = new GoodListModel<>();
        GoodListModel<Vessel> assignedModel = new GoodListModel<>();
        availableVesselsList.setModel(availableModel);
        assignedVesselsList.setModel(assignedModel);
        controller.getVessels().stream() // Available vessels
                .filter(v -> !v.inMission(mission))
                .filter(v -> !v.getStatus().equals(VesselStatus.CRASHED))
                .forEach(availableModel::add);
        mission.getVessels().stream() // Assigned vessels
                .filter(v -> !v.getStatus().equals(VesselStatus.CRASHED))
                .forEach(assignedModel::add);

        // Available vessel listener
        availableVesselsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = availableVesselsList.locationToIndex(e.getPoint());
                if (index < 0 || index > availableModel.getSize()) return;
                Vessel v = availableModel.getElementAt(index);
                availableModel.remove(index);
                assignedModel.add(v);
                availableModel.sort(Comparator.comparing(Vessel::toString));
                assignedModel.sort(Comparator.comparing(Vessel::toString));
                // Update date fields
                KSPDate last;
                if (mission.getEvents().isEmpty()) last = mission.getStart();
                else last = mission.getEvents().get(mission.getEvents().size()).getDate();
                yearTextField.setText(Integer.toString(last.getYear()));
                dayTextField.setText(Integer.toString(last.getDay()));
                hourTextField.setText(Integer.toString(last.getHour()));
                minuteTextField.setText(Integer.toString(last.getMinute()));
                secondTextField.setText(Integer.toString(last.getSecond()));

                assignedModel.getItems().stream().map(Vessel::getCreationDate).forEach(creationDate -> {
                    if (creationDate.after(
                            Integer.parseInt(yearTextField.getText()),
                            Integer.parseInt(dayTextField.getText()),
                            Integer.parseInt(hourTextField.getText()),
                            Integer.parseInt(minuteTextField.getText()),
                            Integer.parseInt(secondTextField.getText())
                    )) {
                        yearTextField.setText(Integer.toString(creationDate.getYear()));
                        dayTextField.setText(Integer.toString(creationDate.getDay()));
                        hourTextField.setText(Integer.toString(creationDate.getHour()));
                        minuteTextField.setText(Integer.toString(creationDate.getMinute()));
                        secondTextField.setText(Integer.toString(creationDate.getSecond()));
                    }
                });
            }
        });

        // Assigned vessel listener
        assignedVesselsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = assignedVesselsList.locationToIndex(e.getPoint());
                if (index < 0 || index > assignedModel.getSize()) return;
                Vessel v = assignedModel.getElementAt(index);
                assignedModel.remove(index);
                availableModel.add(v);
                assignedModel.sort(Comparator.comparing(Vessel::toString));
                availableModel.sort(Comparator.comparing(Vessel::toString));
                // Update date fields
                KSPDate last;
                if (mission.getEvents().isEmpty()) last = mission.getStart();
                else last = mission.getEvents().get(mission.getEvents().size()).getDate();
                yearTextField.setText(Integer.toString(last.getYear()));
                dayTextField.setText(Integer.toString(last.getDay()));
                hourTextField.setText(Integer.toString(last.getHour()));
                minuteTextField.setText(Integer.toString(last.getMinute()));
                secondTextField.setText(Integer.toString(last.getSecond()));

                assignedModel.getItems().stream().map(Vessel::getCreationDate).forEach(creationDate -> {
                    if (creationDate.after(
                            Integer.parseInt(yearTextField.getText()),
                            Integer.parseInt(dayTextField.getText()),
                            Integer.parseInt(hourTextField.getText()),
                            Integer.parseInt(minuteTextField.getText()),
                            Integer.parseInt(secondTextField.getText())
                    )) {
                        yearTextField.setText(Integer.toString(creationDate.getYear()));
                        dayTextField.setText(Integer.toString(creationDate.getDay()));
                        hourTextField.setText(Integer.toString(creationDate.getHour()));
                        minuteTextField.setText(Integer.toString(creationDate.getMinute()));
                        secondTextField.setText(Integer.toString(creationDate.getSecond()));
                    }
                });
            }
        });
    }

    private void logPanelSetup() {
        // Celestial body combo box
        DefaultComboBoxModel<CelestialBody> bodyModel = new DefaultComboBoxModel<>();
        celestialBodyComboBox.setModel(bodyModel);
        bodyModel.addAll(Arrays.asList(CelestialBody.values()));

        // No dynamic editing, no listeners
    }
}
