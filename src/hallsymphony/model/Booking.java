package hallsymphony.model;

/**
 * Booking - records a customer's hall reservation.
 * Calculates cost based on duration × ratePerHour.
 */
public class Booking {

    private String bookingId;
    private String customerId;
    private String hallId;
    private String startDateTime;   // "yyyy-MM-dd HH:mm"
    private String endDateTime;
    private String eventDescription;
    private String status;          // CONFIRMED | CANCELLED | COMPLETED
    private double totalCost;
    private String paymentMethod;
    private String paymentStatus;   // PAID | REFUNDED
    private String createdDate;     // "yyyy-MM-dd"

    public Booking(String bookingId, String customerId, String hallId,
                   String startDateTime, String endDateTime,
                   String eventDescription, String status,
                   double totalCost, String paymentMethod,
                   String paymentStatus, String createdDate) {
        this.bookingId        = bookingId;
        this.customerId       = customerId;
        this.hallId           = hallId;
        this.startDateTime    = startDateTime;
        this.endDateTime      = endDateTime;
        this.eventDescription = eventDescription;
        this.status           = status;
        this.totalCost        = totalCost;
        this.paymentMethod    = paymentMethod;
        this.paymentStatus    = paymentStatus;
        this.createdDate      = createdDate;
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public String getBookingId()        { return bookingId; }
    public String getCustomerId()       { return customerId; }
    public String getHallId()           { return hallId; }
    public String getStartDateTime()    { return startDateTime; }
    public String getEndDateTime()      { return endDateTime; }
    public String getEventDescription() { return eventDescription; }
    public String getStatus()           { return status; }
    public double getTotalCost()        { return totalCost; }
    public String getPaymentMethod()    { return paymentMethod; }
    public String getPaymentStatus()    { return paymentStatus; }
    public String getCreatedDate()      { return createdDate; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setStatus(String s)        { this.status = s; }
    public void setPaymentStatus(String s) { this.paymentStatus = s; }
    public void setTotalCost(double c)     { this.totalCost = c; }

    @Override
    public String toString() {
        return "[" + bookingId + "] Hall:" + hallId
               + " | " + startDateTime + " -> " + endDateTime
               + " | RM " + String.format("%.2f", totalCost)
               + " | " + status;
    }
}
