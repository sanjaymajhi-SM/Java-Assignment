package hallsymphony.model;

/**
 * Issue - a complaint or problem raised by a customer about a booking.
 * Managed and resolved by the Manager and Scheduler.
 */
public class Issue {

    private String issueId;
    private String customerId;
    private String bookingId;
    private String description;
    private String managerResponse;
    private String assignedSchedulerId;
    private String status;          // IN_PROGRESS | DONE | CLOSED | CANCELLED
    private String createdDate;     // "yyyy-MM-dd"

    public Issue(String issueId, String customerId, String bookingId,
                 String description, String managerResponse,
                 String assignedSchedulerId, String status, String createdDate) {
        this.issueId             = issueId;
        this.customerId          = customerId;
        this.bookingId           = bookingId;
        this.description         = description;
        this.managerResponse     = managerResponse;
        this.assignedSchedulerId = assignedSchedulerId;
        this.status              = status;
        this.createdDate         = createdDate;
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public String getIssueId()              { return issueId; }
    public String getCustomerId()           { return customerId; }
    public String getBookingId()            { return bookingId; }
    public String getDescription()          { return description; }
    public String getManagerResponse()      { return managerResponse; }
    public String getAssignedSchedulerId()  { return assignedSchedulerId; }
    public String getStatus()               { return status; }
    public String getCreatedDate()          { return createdDate; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setStatus(String s)               { this.status = s; }
    public void setManagerResponse(String r)      { this.managerResponse = r; }
    public void setAssignedSchedulerId(String id) { this.assignedSchedulerId = id; }

    @Override
    public String toString() {
        return "[" + issueId + "] Booking:" + bookingId
               + " | Status:" + status
               + " | " + description.substring(0, Math.min(30, description.length())) + "...";
    }
}
