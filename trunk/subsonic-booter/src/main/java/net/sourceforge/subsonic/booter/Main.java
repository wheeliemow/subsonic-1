package net.sourceforge.subsonic.booter;

import com.jgoodies.looks.windows.WindowsLookAndFeel;

import javax.swing.*;

/**
 * Application entry point for Subsonic booter.
 *
 * @author Sindre Mehus
 */
public class Main {


    public Main() {

        System.setProperty("javax.swing.adjustPopupLocationToFit", "false");

        // Set look-and-feel.
        try {
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        } catch (Exception x) {
            System.err.println("Failed to set look-and-feel.\n" + x);
        }

        new SubsonicController();
    }

    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            public void run() {
                new Main();
            }
        };

        SwingUtilities.invokeLater(runnable);
    }
}
