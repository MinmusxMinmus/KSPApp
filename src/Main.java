import controller.GUIController;
import gui.MainScreen;
import kerbals.Role;
import vessels.VesselType;

public class Main {
    public static void main(String[] args) {
        GUIController c = new GUIController();
        c.addKerbal("Tester", true, Role.PILOT);
        c.addKerbal("Testina", false, Role.ENGINEER, 5);
        c.addVesselConcept("Penis pincher", VesselType.LANDER);
        MainScreen gui = new MainScreen(c);
        gui.pack();
        gui.setVisible(true);

    }
}
