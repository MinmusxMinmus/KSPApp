import controller.GUIController;
import gui.MainScreen;
import kerbals.Job;
import other.KSPDate;
import vessels.Destination;
import vessels.VesselType;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            GUIController c = new GUIController();

//            c.addKerbalHired("Tester", true, Job.PILOT, new KSPDate(0, 0));
//            c.addKerbalHired("Testina", false, Job.ENGINEER, new KSPDate(0, 0));
//
//            c.addVesselConcept("Penis pincher", VesselType.LANDER, new Destination[]{Destination.KERBIN_LOCAL});
//            c.addVesselInstance(c.getVesselConcept("Penis pincher"));
//            c.addVesselInstance(c.getVesselConcept("Penis pincher"));
//
//            c.saveChanges();

            MainScreen gui = new MainScreen(c);
            gui.pack();
            gui.setVisible(true);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
