package hallsymphony.model;

/**
 * User - Abstract base class (OOP: Abstraction + Inheritance)
 * All user types (Customer, Scheduler, Admin, Manager) extend this class.
 */
public abstract class User {

    // Common fields shared by ALL user types
    protected String userId;
    protected String username;
    protected String password;
    protected String email;
    protected String phone;
    protected String role;
    protected boolean isActive;

    // Constructor used when CREATING a new user
    public User(String userId, String username, String password,
                String email, String phone, String role) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.email    = email;
        this.phone    = phone;
        this.role     = role;
        this.isActive = true;
    }

    // Check if the entered password matches
    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public String  getUserId()   { return userId; }
    public String  getUsername() { return username; }
    public String  getPassword() { return password; }
    public String  getEmail()    { return email; }
    public String  getPhone()    { return phone; }
    public String  getRole()     { return role; }
    public boolean isActive()    { return isActive; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setEmail(String email)       { this.email = email; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setPassword(String password) { this.password = password; }
    public void setActive(boolean active)    { this.isActive = active; }

    // Each subclass must say how to display itself (OOP: Polymorphism)
    @Override
    public String toString() {
        return "[" + role + "] " + username + " | " + email;
    }
}
