import controller.GUIController;
import gui.MainScreen;
import kerbals.Role;

public class Main {
    public static void main(String[] args) {
        GUIController c = new GUIController();
        c.addKerbal("Tester", true, Role.PILOT);
        c.addKerbal("Testina", false, Role.ENGINEER, 5);
        MainScreen gui = new MainScreen(c);
        gui.pack();
        gui.setVisible(true);

    }
}
