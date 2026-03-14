package hallsymphony.ui;

import hallsymphony.data.UserFileManager;
import hallsymphony.model.Customer;
import hallsymphony.util.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * RegisterPanel - lets a new Customer create an account.
 */
public class RegisterPanel extends JPanel {

    private final MainFrame       mainFrame;
    private final UserFileManager userFM = new UserFileManager();

    private JTextField     usernameField, emailField, phoneField, addressField;
    private JPasswordField passwordField, confirmField;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(UIHelper.LIGHT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.makeHeader("Create Account", "Hall Symphony Inc. — Customer Registration"), BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UIHelper.LIGHT_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Create fields
        usernameField = UIHelper.makeField(22);
        emailField    = UIHelper.makeField(22);
        phoneField    = UIHelper.makeField(22);
        passwordField = UIHelper.makePasswordField(22);
        confirmField  = UIHelper.makePasswordField(22);
        addressField  = UIHelper.makeField(22);

        // Add rows: label in col 0, field in col 1
        String[]     labels = { "Username *", "Email *", "Phone *", "Password *", "Confirm Password *", "Address" };
        JComponent[] fields = { usernameField, emailField, phoneField, passwordField, confirmField, addressField };

        for (int i = 0; i < labels.length; i++) {
            GridBagConstraints lc = UIHelper.gbc(0, i);
            lc.weightx = 0;
            form.add(UIHelper.boldLabel(labels[i]), lc);

            GridBagConstraints fc = UIHelper.gbc(1, i);
            fc.weightx = 1;
            form.add(fields[i], fc);
        }

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);
        JButton registerBtn = UIHelper.primaryBtn("Register");
        JButton backBtn     = UIHelper.secondaryBtn("Back to Login");
        btnRow.add(registerBtn);
        btnRow.add(backBtn);

        GridBagConstraints bc = UIHelper.gbcWide(labels.length);
        bc.insets = new Insets(18, 8, 8, 8);
        form.add(btnRow, bc);

        wrapper.add(form);
        add(wrapper, BorderLayout.CENTER);

        registerBtn.addActionListener(e -> doRegister());
        backBtn.addActionListener(e -> mainFrame.showCard(MainFrame.LOGIN_CARD));
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());
        String address  = addressField.getText().trim();

        // Basic validation
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            UIHelper.showError(this, "Please fill in all required fields (*).");
            return;
        }
        if (!password.equals(confirm)) {
            UIHelper.showError(this, "Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            UIHelper.showError(this, "Password must be at least 6 characters.");
            return;
        }
        if (userFM.usernameExists(username)) {
            UIHelper.showError(this, "Username already taken. Please choose another.");
            return;
        }

        // Create new customer and save to users.txt
        // company is passed as empty string since we removed that field
        String newId = userFM.generateUserId("CUSTOMER");
        Customer newCustomer = new Customer(newId, username, password,
                email, phone, address, true);
        userFM.addUser(newCustomer);

        UIHelper.showSuccess(this, "Account created! You can now log in.");
        mainFrame.showCard(MainFrame.LOGIN_CARD);
    }
}