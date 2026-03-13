package hallsymphony.data;

import hallsymphony.model.Booking;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingFileManager - reads and writes bookings.txt
 *
 * FILE FORMAT (bookings.txt):
 *   bookingId|customerId|hallId|startDateTime|endDateTime|eventDesc|status|totalCost|paymentMethod|paymentStatus|createdDate
 *
 * Example:
 *   BKG-001|USR-001|HLL-002|2025-07-10 09:00|2025-07-10 13:00|Wedding Dinner|CONFIRMED|400.00|Cash|PAID|2025-06-15
 */
public class BookingFileManager {

    private static final String FILE = "bookings.txt";

    // ── Read ──────────────────────────────────────────────────────────────

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();

        for (String line : FileHelper.readAllLines(FILE)) {
            Booking b = parseLine(line);
            if (b != null) list.add(b);
        }

        return list;
    }

    private Booking parseLine(String line) {
        String[] f = FileHelper.splitLine(line);
        if (f.length < 11) return null;

        return new Booking(
            FileHelper.getField(f, 0),                       // bookingId
            FileHelper.getField(f, 1),                       // customerId
            FileHelper.getField(f, 2),                       // hallId
            FileHelper.getField(f, 3),                       // startDateTime
            FileHelper.getField(f, 4),                       // endDateTime
            FileHelper.getField(f, 5),                       // eventDescription
            FileHelper.getField(f, 6),                       // status
            Double.parseDouble(FileHelper.getField(f, 7)),   // totalCost
            FileHelper.getField(f, 8),                       // paymentMethod
            FileHelper.getField(f, 9),                       // paymentStatus
            FileHelper.getField(f, 10)                       // createdDate
        );
    }

    public Booking findById(String bookingId) {
        for (Booking b : getAllBookings()) {
            if (b.getBookingId().equals(bookingId)) return b;
        }
        return null;
    }

    // Get all bookings for a specific customer
    public List<Booking> getByCustomer(String customerId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : getAllBookings()) {
            if (b.getCustomerId().equals(customerId)) result.add(b);
        }
        return result;
    }

    // ── Add ───────────────────────────────────────────────────────────────

    public void addBooking(Booking booking) {
        FileHelper.appendLine(FILE, buildLine(booking));
    }

    // ── Update ────────────────────────────────────────────────────────────

    public void updateBooking(Booking updated) {
        List<String> lines    = FileHelper.readAllLines(FILE);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            if (FileHelper.getField(f, 0).equals(updated.getBookingId())) {
                newLines.add(buildLine(updated));
            } else {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Build line ────────────────────────────────────────────────────────

    private String buildLine(Booking b) {
        return FileHelper.joinFields(
            b.getBookingId(),
            b.getCustomerId(),
            b.getHallId(),
            b.getStartDateTime(),
            b.getEndDateTime(),
            b.getEventDescription(),
            b.getStatus(),
            String.format("%.2f", b.getTotalCost()),
            b.getPaymentMethod(),
            b.getPaymentStatus(),
            b.getCreatedDate()
        );
    }

    // ── Generate ID ───────────────────────────────────────────────────────

    public String generateBookingId() {
        int next = getAllBookings().size() + 1;
        return "BKG-" + String.format("%03d", next);
    }

    // ── Calculate cost helper ─────────────────────────────────────────────

    /**
     * Calculates booking cost.
     * Times must be in format "yyyy-MM-dd HH:mm"
     */
    public static double calculateCost(String startDateTime, String endDateTime, double ratePerHour) {
        try {
            // Parse "yyyy-MM-dd HH:mm"
            String[] startParts = startDateTime.split(" ");
            String[] endParts   = endDateTime.split(" ");

            String[] startTime = startParts[1].split(":");
            String[] endTime   = endParts[1].split(":");

            // Simple same-day calculation
            int startMinutes = Integer.parseInt(startTime[0]) * 60 + Integer.parseInt(startTime[1]);
            int endMinutes   = Integer.parseInt(endTime[0])   * 60 + Integer.parseInt(endTime[1]);

            // Handle multi-day: count day difference
            java.time.LocalDate startDate = java.time.LocalDate.parse(startParts[0]);
            java.time.LocalDate endDate   = java.time.LocalDate.parse(endParts[0]);
            long dayDiff = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

            double totalMinutes = (dayDiff * 24 * 60) + (endMinutes - startMinutes);
            double hours = totalMinutes / 60.0;

            return Math.max(0, hours * ratePerHour);
        } catch (Exception e) {
            return 0;
        }
    }
}
