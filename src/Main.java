import hallsymphony.data.DatabaseSeeder;
import hallsymphony.ui.MainFrame;

import javax.swing.*;

/**
 * Main - entry point of the Hall Symphony Booking System.
 *
 * Steps:
 *  1. Seed default data into TXT files (only on first run)
 *  2. Launch the Login window
 */
public class Main {
    public static void main(String[] args) {
        // Step 1: Create default TXT database files if they don't exist
        DatabaseSeeder.seedIfEmpty();

        // Step 2: Launch the GUI on the Swing event thread
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
