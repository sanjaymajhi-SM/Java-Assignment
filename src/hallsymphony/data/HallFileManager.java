package hallsymphony.data;

import hallsymphony.model.Hall;
import java.util.ArrayList;
import java.util.List;

/**
 * HallFileManager - reads and writes halls.txt
 *
 * FILE FORMAT (halls.txt) - one hall per line:
 *   hallId|hallName|hallType|capacity|ratePerHour|description
 *
 * Example:
 *   HLL-001|Grand Auditorium|AUDITORIUM|1000|300.00|Main hall for large events
 *   HLL-002|Crystal Banquet|BANQUET_HALL|300|100.00|Elegant banquet hall
 *   HLL-003|Meeting Room A|MEETING_ROOM|30|50.00|Small meeting room
 */
public class HallFileManager {

    private static final String FILE = "halls.txt";

    // ── Read ──────────────────────────────────────────────────────────────

    public List<Hall> getAllHalls() {
        List<Hall> halls = new ArrayList<>();

        for (String line : FileHelper.readAllLines(FILE)) {
            Hall hall = parseLine(line);
            if (hall != null) halls.add(hall);
        }

        return halls;
    }

    private Hall parseLine(String line) {
        String[] f = FileHelper.splitLine(line);
        if (f.length < 6) return null;

        return new Hall(
            FileHelper.getField(f, 0),                          // hallId
            FileHelper.getField(f, 1),                          // hallName
            FileHelper.getField(f, 2),                          // hallType
            Integer.parseInt(FileHelper.getField(f, 3)),        // capacity
            Double.parseDouble(FileHelper.getField(f, 4)),      // ratePerHour
            FileHelper.getField(f, 5)                           // description
        );
    }

    public Hall findById(String hallId) {
        for (Hall h : getAllHalls()) {
            if (h.getHallId().equals(hallId)) return h;
        }
        return null;
    }

    // ── Add ───────────────────────────────────────────────────────────────

    public void addHall(Hall hall) {
        FileHelper.appendLine(FILE, buildLine(hall));
    }

    // ── Update ────────────────────────────────────────────────────────────

    public void updateHall(Hall updated) {
        List<String> lines     = FileHelper.readAllLines(FILE);
        List<String> newLines  = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            if (FileHelper.getField(f, 0).equals(updated.getHallId())) {
                newLines.add(buildLine(updated));
            } else {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public void deleteHall(String hallId) {
        List<String> lines    = FileHelper.readAllLines(FILE);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            if (!FileHelper.getField(f, 0).equals(hallId)) {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Build line from Hall object ───────────────────────────────────────

    private String buildLine(Hall h) {
        return FileHelper.joinFields(
            h.getHallId(),
            h.getHallName(),
            h.getHallType(),
            String.valueOf(h.getCapacity()),
            String.format("%.2f", h.getRatePerHour()),
            h.getDescription()
        );
    }

    // ── Generate ID ───────────────────────────────────────────────────────

    public String generateHallId() {
        int next = getAllHalls().size() + 1;
        return "HLL-" + String.format("%03d", next);
    }

    // ── Rate lookup by type ───────────────────────────────────────────────

    public static double getRateForType(String hallType) {
        switch (hallType.toUpperCase()) {
            case "AUDITORIUM":   return 300.00;
            case "BANQUET_HALL": return 100.00;
            case "MEETING_ROOM": return  50.00;
            default:             return  50.00;
        }
    }

    public static int getCapacityForType(String hallType) {
        switch (hallType.toUpperCase()) {
            case "AUDITORIUM":   return 1000;
            case "BANQUET_HALL": return 300;
            case "MEETING_ROOM": return 30;
            default:             return 30;
        }
    }
}
