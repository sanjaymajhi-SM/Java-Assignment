package hallsymphony.data;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DatabaseSeeder - Creates default data in TXT files when the app runs for the first time.
 * Only seeds if the files don't already exist.
 */
public class DatabaseSeeder {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void seedIfEmpty() {
        new File(FileHelper.DB_FOLDER).mkdirs();
        seedUsers();
        seedHalls();
        seedSchedules();
    }

    private static void seedUsers() {
        File f = new File(FileHelper.DB_FOLDER + "users.txt");
        if (f.exists()) return;

        String[] defaultUsers = {
                "STF-001|admin|admin123|admin@hallsymphony.com|0100000001|ADMIN|true|Administration",
                "STF-002|manager|manager123|manager@hallsymphony.com|0100000002|MANAGER|true|Management",
                "STF-003|scheduler1|sched123|scheduler1@hallsymphony.com|0100000003|SCHEDULER|true|Hall Operations",
                "STF-004|scheduler2|sched456|scheduler2@hallsymphony.com|0100000004|SCHEDULER|true|Hall Operations",
                "USR-001|Sanjay|sanjay123|sanjay@email.com|0123456789|CUSTOMER|true|KTM|"

        };

        for (String line : defaultUsers) FileHelper.appendLine("users.txt", line);
        System.out.println("Seeded default users.");
    }

    private static void seedHalls() {
        File f = new File(FileHelper.DB_FOLDER + "halls.txt");
        if (f.exists()) return;

        String[] defaultHalls = {
                "HLL-001|Grand Auditorium|AUDITORIUM|1000|300.00|Spacious main auditorium for large conferences and events",
                "HLL-002|Crystal Banquet Hall|BANQUET_HALL|300|100.00|Elegant banquet hall ideal for weddings and dinners",
                "HLL-003|Meeting Room A|MEETING_ROOM|30|50.00|Cosy meeting room with projector and whiteboard",
                "HLL-004|Meeting Room B|MEETING_ROOM|30|50.00|Meeting room with video conferencing facilities"
        };

        for (String line : defaultHalls) FileHelper.appendLine("halls.txt", line);
        System.out.println("Seeded default halls.");
    }

    private static void seedSchedules() {
        File f = new File(FileHelper.DB_FOLDER + "schedules.txt");
        if (f.exists()) return;

        // Availability window: today 08:00 → 6 months from now 18:00
        String avStart = LocalDate.now().atTime(8, 0).format(FMT);
        String avEnd   = LocalDate.now().plusMonths(6).atTime(18, 0).format(FMT);

        String[] defaultSchedules = {
                "SCH-001|HLL-001|AVAILABILITY|" + avStart + "|" + avEnd + "|Open for bookings|STF-003",
                "SCH-002|HLL-002|AVAILABILITY|" + avStart + "|" + avEnd + "|Open for bookings|STF-003",
                "SCH-003|HLL-003|AVAILABILITY|" + avStart + "|" + avEnd + "|Open for bookings|STF-004",
                "SCH-004|HLL-004|AVAILABILITY|" + avStart + "|" + avEnd + "|Open for bookings|STF-004"
        };

        for (String line : defaultSchedules) FileHelper.appendLine("schedules.txt", line);
        System.out.println("Seeded default schedules.");
    }
}