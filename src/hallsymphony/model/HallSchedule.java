package hallsymphony.model;

/**
 * HallSchedule - stores availability or maintenance windows for a hall.
 * scheduleType: AVAILABILITY or MAINTENANCE
 */
public class HallSchedule {

    private String scheduleId;
    private String hallId;
    private String scheduleType;   // AVAILABILITY | MAINTENANCE
    private String startDateTime;  // stored as string "yyyy-MM-dd HH:mm"
    private String endDateTime;
    private String remarks;
    private String createdByStaffId;

    public HallSchedule(String scheduleId, String hallId, String scheduleType,
                        String startDateTime, String endDateTime,
                        String remarks, String createdByStaffId) {
        this.scheduleId       = scheduleId;
        this.hallId           = hallId;
        this.scheduleType     = scheduleType;
        this.startDateTime    = startDateTime;
        this.endDateTime      = endDateTime;
        this.remarks          = remarks;
        this.createdByStaffId = createdByStaffId;
    }

    public String getScheduleId()       { return scheduleId; }
    public String getHallId()           { return hallId; }
    public String getScheduleType()     { return scheduleType; }
    public String getStartDateTime()    { return startDateTime; }
    public String getEndDateTime()      { return endDateTime; }
    public String getRemarks()          { return remarks; }
    public String getCreatedByStaffId() { return createdByStaffId; }

    public void setStartDateTime(String s) { this.startDateTime = s; }
    public void setEndDateTime(String e)   { this.endDateTime = e; }
    public void setRemarks(String r)       { this.remarks = r; }

    @Override
    public String toString() {
        return "[" + scheduleType + "] Hall:" + hallId
               + " | " + startDateTime + " -> " + endDateTime
               + (remarks.isEmpty() ? "" : " | " + remarks);
    }
}
