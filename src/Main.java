import controller.GUIController;
import gui.MainScreen;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("WARNING: No arguments! Please input the database filename (without the .c)");
            return;
        }
        try {
            GUIController c = new GUIController(args[0]);

            MainScreen gui = new MainScreen(c);
            gui.pack();
            gui.setVisible(true);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
