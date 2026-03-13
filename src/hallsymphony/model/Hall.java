package hallsymphony.model;

/**
 * Hall - represents a bookable hall.
 * Hall types: AUDITORIUM, BANQUET_HALL, MEETING_ROOM
 */
public class Hall {

    private String hallId;
    private String hallName;
    private String hallType;     // AUDITORIUM | BANQUET_HALL | MEETING_ROOM
    private int    capacity;
    private double ratePerHour;
    private String description;

    public Hall(String hallId, String hallName, String hallType,
                int capacity, double ratePerHour, String description) {
        this.hallId      = hallId;
        this.hallName    = hallName;
        this.hallType    = hallType;
        this.capacity    = capacity;
        this.ratePerHour = ratePerHour;
        this.description = description;
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public String getHallId()       { return hallId; }
    public String getHallName()     { return hallName; }
    public String getHallType()     { return hallType; }
    public int    getCapacity()     { return capacity; }
    public double getRatePerHour()  { return ratePerHour; }
    public String getDescription()  { return description; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setHallName(String n)       { this.hallName = n; }
    public void setHallType(String t)       { this.hallType = t; }
    public void setCapacity(int c)          { this.capacity = c; }
    public void setRatePerHour(double r)    { this.ratePerHour = r; }
    public void setDescription(String d)    { this.description = d; }

    @Override
    public String toString() {
        return hallName + " (" + hallType + ") | Cap: " + capacity
               + " | RM " + String.format("%.2f", ratePerHour) + "/hr";
    }
}
