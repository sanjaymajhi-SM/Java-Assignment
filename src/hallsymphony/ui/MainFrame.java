package hallsymphony.ui;

import hallsymphony.data.*;
import hallsymphony.model.*;
import hallsymphony.util.UIHelper;
import hallsymphony.ui.scheduler.SchedulerDashboard;
import hallsymphony.ui.customer.CustomerDashboard;
import hallsymphony.ui.admin.AdminDashboard;
import hallsymphony.ui.manager.ManagerDashboard;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame - the root window of the application.
 * Uses CardLayout to switch between Login and Register screens.
 * After login, opens the correct role dashboard.
 */
public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;

    public static final String LOGIN_CARD    = "LOGIN";
    public static final String REGISTER_CARD = "REGISTER";

    public MainFrame() {
        setTitle("Hall Symphony Inc. — Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        cardPanel.add(new LoginPanel(this),    LOGIN_CARD);
        cardPanel.add(new RegisterPanel(this), REGISTER_CARD);

        add(cardPanel);
        showCard(LOGIN_CARD);
        setVisible(true);
    }

    public void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }

    // After a successful login, close this window and open the right dashboard
    public void openDashboard(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            switch (user.getRole()) {
                case "SCHEDULER": new SchedulerDashboard((Staff) user); break;
                case "CUSTOMER":  new CustomerDashboard((Customer) user); break;
                case "ADMIN":     new AdminDashboard((Staff) user); break;
                case "MANAGER":   new ManagerDashboard((Staff) user); break;
            }
        });
    }

    // Called when a user logs out from any dashboard
    public static void logout() {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
