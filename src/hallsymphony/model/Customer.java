package hallsymphony.model;

/**
 * Customer - extends User (OOP: Inheritance)
 * Has extra fields: address and company.
 */
public class Customer extends User {

    private String address;

    public Customer(String userId, String username, String password,
                    String email, String phone,
                    String address, boolean isActive) {
        super(userId, username, password, email, phone, "CUSTOMER");
        this.address  = address;
        this.isActive = isActive;
    }

    public String getAddress()         { return address; }
    public void   setAddress(String a) { this.address = a; }


    @Override
    public String toString() {
        return "[CUSTOMER] " + username + " | " + email + " | ";
    }
}
