package hallsymphony.data;

import hallsymphony.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserFileManager - reads and writes users.txt
 *
 * Handles reading and writing of users.txt
 *
 * FILE FORMAT (users.txt) - one user per line:
 *   userId|username|password|email|phone|role|isActive|field1|field2
 *
 * For CUSTOMER:
 *   USR-001|john|pass123|john@email.com|0123456|CUSTOMER|true|123 Main St|Acme Corp
 *
 * For STAFF (Scheduler, Admin, Manager):
 *   STF-001|admin|pass123|admin@hs.com|0100001|ADMIN|true|Administration
 */
public class UserFileManager {

    private static final String FILE = "users.txt";

    // ── Read all users from file ──────────────────────────────────────────

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        for (String line : FileHelper.readAllLines(FILE)) {
            User user = parseLine(line);
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }

    // Convert one line of text into a User object
    private User parseLine(String line) {
        String[] f = FileHelper.splitLine(line);

        // Need at least 7 fields to be valid
        if (f.length < 7) return null;

        String userId   = FileHelper.getField(f, 0);
        String username = FileHelper.getField(f, 1);
        String password = FileHelper.getField(f, 2);
        String email    = FileHelper.getField(f, 3);
        String phone    = FileHelper.getField(f, 4);
        String role     = FileHelper.getField(f, 5);
        boolean active  = FileHelper.getField(f, 6).equalsIgnoreCase("true");

        // Build the correct subclass based on role
        if (role.equals("CUSTOMER")) {
            String address = FileHelper.getField(f, 7);
            String company = FileHelper.getField(f, 8);
            return new Customer(userId, username, password, email, phone,
                                address, company, active);
        } else {
            // ADMIN, SCHEDULER, MANAGER all use Staff
            String department = FileHelper.getField(f, 7);
            return new Staff(userId, username, password, email, phone,
                             role, department, active);
        }
    }

    // ── Find a specific user ──────────────────────────────────────────────

    public User findByUsername(String username) {
        for (User u : getAllUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public User findById(String userId) {
        for (User u : getAllUsers()) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    // Login: check username + password
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.checkPassword(password) && user.isActive()) {
            return user;
        }
        return null;
    }

    // Get all users by role
    public List<User> getByRole(String role) {
        List<User> result = new ArrayList<>();
        for (User u : getAllUsers()) {
            if (u.getRole().equals(role)) result.add(u);
        }
        return result;
    }

    // ── Add a new user ────────────────────────────────────────────────────

    public void addUser(User user) {
        String line = buildLine(user);
        FileHelper.appendLine(FILE, line);
    }

    // ── Update an existing user ───────────────────────────────────────────

    public void updateUser(User updatedUser) {
        List<String> lines = FileHelper.readAllLines(FILE);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            // Replace the line if userId matches
            if (FileHelper.getField(f, 0).equals(updatedUser.getUserId())) {
                newLines.add(buildLine(updatedUser));
            } else {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Delete a user ─────────────────────────────────────────────────────

    public void deleteUser(String userId) {
        List<String> lines = FileHelper.readAllLines(FILE);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            // Keep all lines EXCEPT the one matching userId
            if (!FileHelper.getField(f, 0).equals(userId)) {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Convert a User object into a text line ────────────────────────────

    private String buildLine(User user) {
        String base = FileHelper.joinFields(
            user.getUserId(),
            user.getUsername(),
            user.getPassword(),
            user.getEmail(),
            user.getPhone(),
            user.getRole(),
            String.valueOf(user.isActive())
        );

        if (user instanceof Customer) {
            Customer c = (Customer) user;
            return base + "|" + c.getAddress() + "|" + c.getCompany();
        } else if (user instanceof Staff) {
            Staff s = (Staff) user;
            return base + "|" + s.getDepartment();
        }

        return base;
    }

    // ── Generate a unique ID ──────────────────────────────────────────────

    public String generateUserId(String role) {
        String prefix = role.equals("CUSTOMER") ? "USR" : "STF";
        List<User> all = getAllUsers();
        int nextNum = all.size() + 1;
        return prefix + "-" + String.format("%03d", nextNum);
    }
}
