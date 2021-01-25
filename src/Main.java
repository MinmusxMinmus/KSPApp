import controller.GUIController;
import gui.MainScreen;
import kerbals.Role;
import other.KSPDate;
import vessels.VesselDestination;
import vessels.VesselType;

public class Main {
    public static void main(String[] args) {
        GUIController c = new GUIController();

        c.addKerbalHired("Tester", true, Role.PILOT, new KSPDate(0, 0));
        c.addKerbalHired("Testina", false, Role.ENGINEER, new KSPDate(0, 0));

        c.addVesselConcept("Penis pincher", VesselType.LANDER, new VesselDestination[]{VesselDestination.KERBIN_LOCAL});
        c.addVesselInstance(c.getVesselConcept("Penis pincher"));
        c.addVesselInstance(c.getVesselConcept("Penis pincher"));
        MainScreen gui = new MainScreen(c);
        gui.pack();
        gui.setVisible(true);
    }
}
