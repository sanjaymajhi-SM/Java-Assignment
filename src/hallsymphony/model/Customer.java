package hallsymphony.model;

/**
 * Customer - extends User (OOP: Inheritance)
 * Has extra fields: address and company.
 */
public class Customer extends User {

    private String address;
    private String company;

    public Customer(String userId, String username, String password,
                    String email, String phone,
                    String address, String company, boolean isActive) {
        super(userId, username, password, email, phone, "CUSTOMER");
        this.address  = address;
        this.company  = company;
        this.isActive = isActive;
    }

    public String getAddress()         { return address; }
    public String getCompany()         { return company; }
    public void   setAddress(String a) { this.address = a; }
    public void   setCompany(String c) { this.company = c; }

    @Override
    public String toString() {
        return "[CUSTOMER] " + username + " | " + email + " | " + company;
    }
}
