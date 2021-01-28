import controller.GUIController;
import gui.MainScreen;
import kerbals.Job;
import other.Destination;
import other.KSPDate;
import vessels.VesselType;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            GUIController c = new GUIController();
            Random random = new Random();

//            c.createKerbalHired("Tester", true, false, Job.PILOT, new KSPDate(c, 0, 0));
//            c.createKerbalHired("Testina", false, true, Job.ENGINEER, new KSPDate(c, 0, 0));
//
//            c.createConcept("Penis pincher", VesselType.LANDER, null, new KSPDate(c, 0, 0, OffsetDateTime.now()), new Destination[]{Destination.KERBIN_LOCAL});
//
//            c.createInstance(c.getConcept("Penis pincher"), random.nextInt());
//            c.createInstance(c.getConcept("Penis pincher"), random.nextInt());
//
//            long id = c.createInstance(c.getConcept("Penis pincher"), random.nextInt());
//
//            c.getInstance(id).destroyed("Jeb forgot to close the hatch");
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
