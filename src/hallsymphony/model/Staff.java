package hallsymphony.model;

/**
 * Staff - extends User (OOP: Inheritance)
 * Base class for Scheduler, Admin, and Manager.
 */
public class Staff extends User {

    private String department;

    public Staff(String userId, String username, String password,
                 String email, String phone,
                 String role, String department, boolean isActive) {
        super(userId, username, password, email, phone, role);
        this.department = department;
        this.isActive   = isActive;
    }

    public String getDepartment()         { return department; }
    public void   setDepartment(String d) { this.department = d; }

    @Override
    public String toString() {
        return "[" + role + "] " + username + " | " + department;
    }
}
