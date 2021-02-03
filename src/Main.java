import controller.GUIController;
import gui.MainScreen;

import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            GUIController c = new GUIController();

            MainScreen gui = new MainScreen(c);
            gui.pack();
            gui.setVisible(true);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
