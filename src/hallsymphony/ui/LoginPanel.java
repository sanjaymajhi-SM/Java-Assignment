package hallsymphony.ui;

import hallsymphony.data.UserFileManager;
import hallsymphony.model.User;
import hallsymphony.util.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * LoginPanel - the login screen shown at startup.
 *
 * User selects their role from a dropdown, then enters
 * username and password. The system checks that the
 * role matches what is stored in the database.
 */
public class LoginPanel extends JPanel {

    private final MainFrame       mainFrame;
    private final UserFileManager userFM = new UserFileManager();

    private JComboBox<String> roleCombo;
    private JTextField        usernameField;
    private JPasswordField    passwordField;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(UIHelper.LIGHT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {

        // ── LEFT: blue branding panel ────────────────────────────────────
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UIHelper.DARK_BLUE);
        brandPanel.setPreferredSize(new Dimension(270, 0));
        brandPanel.setBorder(BorderFactory.createEmptyBorder(60, 30, 30, 30));

        JLabel appIcon = new JLabel("HS");
        appIcon.setFont(new Font("Arial", Font.BOLD, 56));
        appIcon.setForeground(Color.WHITE);
        appIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel appName = new JLabel("HALL SYMPHONY");
        appName.setFont(new Font("Arial", Font.BOLD, 17));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel appSub = new JLabel("Booking System");
        appSub.setFont(new Font("Arial", Font.PLAIN, 13));
        appSub.setForeground(new Color(180, 210, 255));
        appSub.setAlignmentX(CENTER_ALIGNMENT);

        // default accounts cheat-sheet
        JTextArea accounts = new JTextArea();
        accounts.setFont(new Font("Arial", Font.PLAIN, 11));
        accounts.setForeground(new Color(160, 200, 255));
        accounts.setBackground(UIHelper.DARK_BLUE);
        accounts.setEditable(false);
        accounts.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        brandPanel.add(Box.createVerticalGlue());
        brandPanel.add(appIcon);
        brandPanel.add(Box.createVerticalStrut(8));
        brandPanel.add(appName);
        brandPanel.add(appSub);
        brandPanel.add(accounts);
        brandPanel.add(Box.createVerticalGlue());

        add(brandPanel, BorderLayout.WEST);

        // ── RIGHT: login form ────────────────────────────────────────────
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setBackground(UIHelper.LIGHT_BG);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // title
        JLabel heading = new JLabel("Sign In");
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(UIHelper.DARK_BLUE);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Select your role, then enter your username and password.");
        hint.setFont(new Font("Arial", Font.PLAIN, 12));
        hint.setForeground(Color.GRAY);
        hint.setAlignmentX(LEFT_ALIGNMENT);

        // role dropdown
        roleCombo = new JComboBox<>(new String[]{
                "Customer", "Scheduler", "Admin", "Manager"
        });
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setMaximumSize(new Dimension(320, 36));
        roleCombo.setAlignmentX(LEFT_ALIGNMENT);

        // username
        usernameField = UIHelper.makeField(22);
        usernameField.setMaximumSize(new Dimension(320, 36));
        usernameField.setAlignmentX(LEFT_ALIGNMENT);

        // password
        passwordField = UIHelper.makePasswordField(22);
        passwordField.setMaximumSize(new Dimension(320, 36));
        passwordField.setAlignmentX(LEFT_ALIGNMENT);

        // login button
        JButton loginBtn = UIHelper.primaryBtn("Login");
        loginBtn.setMaximumSize(new Dimension(320, 40));
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);

        // register link (for new customers)
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        regRow.setOpaque(false);
        regRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel regText = new JLabel("New customer?  ");
        regText.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton regLink = new JButton("Create an account");
        regLink.setFont(new Font("Arial", Font.BOLD, 12));
        regLink.setBorderPainted(false);
        regLink.setContentAreaFilled(false);
        regLink.setForeground(UIHelper.MID_BLUE);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        regRow.add(regText);
        regRow.add(regLink);

        // put form together
        form.add(heading);
        form.add(Box.createVerticalStrut(6));
        form.add(hint);
        form.add(Box.createVerticalStrut(28));

        form.add(fieldLabel("Role"));
        form.add(Box.createVerticalStrut(5));
        form.add(roleCombo);
        form.add(Box.createVerticalStrut(16));

        form.add(fieldLabel("Username"));
        form.add(Box.createVerticalStrut(5));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(16));

        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(5));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(24));

        form.add(loginBtn);
        form.add(Box.createVerticalStrut(14));
        form.add(regRow);

        rightWrapper.add(form);
        add(rightWrapper, BorderLayout.CENTER);

        // ── wire up actions ──────────────────────────────────────────────
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());   // press Enter in password = login
        regLink.addActionListener(e -> mainFrame.showCard(MainFrame.REGISTER_CARD));
    }

    // ─────────────────────────────────────────────────────────────────────
    // Login logic
    // ─────────────────────────────────────────────────────────────────────
    private void doLogin() {

        // 1. read what the user typed
        String selectedRole = (String) roleCombo.getSelectedItem();
        String username     = usernameField.getText().trim();
        String password     = new String(passwordField.getPassword());

        // 2. basic empty-check
        if (username.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please enter both username and password.");
            return;
        }

        // 3. look up the account
        User user = userFM.login(username, password);

        // 4. account not found or wrong password or blocked
        if (user == null) {
            UIHelper.showError(this,
                    "Login failed.\n\nReason: username/password is incorrect, or the account is disabled.");
            return;
        }

        // 5. convert selected role text to the role code stored in users.txt
        //    e.g. "Admin" -> "ADMIN",  "Scheduler" -> "SCHEDULER"
        String expectedRole = selectedRole.toUpperCase();

        // 6. check that the role matches
        if (!user.getRole().equals(expectedRole)) {
            UIHelper.showError(this,
                    "Wrong role selected!\n\n"
                            + "You selected: " + selectedRole + "\n"
                            + "This account is: " + user.getRole() + "\n\n"
                            + "Please choose the correct role from the dropdown.");
            return;
        }

        // 7. all good – open the correct dashboard
        mainFrame.openDashboard(user);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Small helper – creates a bold label aligned left
    // ─────────────────────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }
}