package hallsymphony.data;

import hallsymphony.model.HallSchedule;
import java.util.ArrayList;
import java.util.List;

/**
 * ScheduleFileManager - reads and writes schedules.txt
 *
 * FILE FORMAT (schedules.txt):
 *   scheduleId|hallId|scheduleType|startDateTime|endDateTime|remarks|createdByStaffId
 *
 * Example:
 *   SCH-001|HLL-001|AVAILABILITY|2025-07-01 08:00|2025-07-31 18:00|Open for July|STF-001
 *   SCH-002|HLL-001|MAINTENANCE|2025-07-15 08:00|2025-07-15 12:00|AC check|STF-001
 */
public class ScheduleFileManager {

    private static final String FILE = "schedules.txt";

    // ── Read ──────────────────────────────────────────────────────────────

    public List<HallSchedule> getAllSchedules() {
        List<HallSchedule> list = new ArrayList<>();

        for (String line : FileHelper.readAllLines(FILE)) {
            HallSchedule s = parseLine(line);
            if (s != null) list.add(s);
        }

        return list;
    }

    private HallSchedule parseLine(String line) {
        String[] f = FileHelper.splitLine(line);
        if (f.length < 7) return null;

        return new HallSchedule(
            FileHelper.getField(f, 0),   // scheduleId
            FileHelper.getField(f, 1),   // hallId
            FileHelper.getField(f, 2),   // scheduleType
            FileHelper.getField(f, 3),   // startDateTime
            FileHelper.getField(f, 4),   // endDateTime
            FileHelper.getField(f, 5),   // remarks
            FileHelper.getField(f, 6)    // createdByStaffId
        );
    }

    // Get all schedules for a specific hall
    public List<HallSchedule> getSchedulesForHall(String hallId) {
        List<HallSchedule> result = new ArrayList<>();
        for (HallSchedule s : getAllSchedules()) {
            if (s.getHallId().equals(hallId)) result.add(s);
        }
        return result;
    }

    // Get all AVAILABILITY schedules for a hall
    public List<HallSchedule> getAvailabilityForHall(String hallId) {
        List<HallSchedule> result = new ArrayList<>();
        for (HallSchedule s : getSchedulesForHall(hallId)) {
            if (s.getScheduleType().equals("AVAILABILITY")) result.add(s);
        }
        return result;
    }

    public HallSchedule findById(String scheduleId) {
        for (HallSchedule s : getAllSchedules()) {
            if (s.getScheduleId().equals(scheduleId)) return s;
        }
        return null;
    }

    // ── Add ───────────────────────────────────────────────────────────────

    public void addSchedule(HallSchedule schedule) {
        FileHelper.appendLine(FILE, buildLine(schedule));
    }

    // ── Update ────────────────────────────────────────────────────────────

    public void updateSchedule(HallSchedule updated) {
        List<String> lines    = FileHelper.readAllLines(FILE);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            if (FileHelper.getField(f, 0).equals(updated.getScheduleId())) {
                newLines.add(buildLine(updated));
            } else {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public void deleteSchedule(String scheduleId) {
        List<String> lines    = FileHelper.readAllLines(FILE);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            if (!FileHelper.getField(f, 0).equals(scheduleId)) {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Build line ────────────────────────────────────────────────────────

    private String buildLine(HallSchedule s) {
        return FileHelper.joinFields(
            s.getScheduleId(),
            s.getHallId(),
            s.getScheduleType(),
            s.getStartDateTime(),
            s.getEndDateTime(),
            s.getRemarks() != null ? s.getRemarks() : "",
            s.getCreatedByStaffId()
        );
    }

    // ── Generate ID ───────────────────────────────────────────────────────

    public String generateScheduleId() {
        int next = getAllSchedules().size() + 1;
        return "SCH-" + String.format("%03d", next);
    }
}
