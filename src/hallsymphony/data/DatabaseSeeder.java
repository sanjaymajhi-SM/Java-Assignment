package hallsymphony.data;

import java.io.File;

/**
 * DatabaseSeeder - Creates default data in TXT files when the app runs for the first time.
 * Only seeds if the files don't already exist.
 */
public class DatabaseSeeder {

    public static void seedIfEmpty() {
        new File(FileHelper.DB_FOLDER).mkdirs();
        seedUsers();
        seedHalls();
    }

    private static void seedUsers() {
        File f = new File(FileHelper.DB_FOLDER + "users.txt");
        if (f.exists()) return;  // Already seeded, skip

        // Format: userId|username|password|email|phone|role|isActive|extra fields
        // NOTE: passwords are stored as plain text here for simplicity.
        //       In production you would store hashed passwords.
        String[] defaultUsers = {
            // Administrators
            "STF-001|admin|admin123|admin@hallsymphony.com|0100000001|ADMIN|true|Administration",

            // Managers
            "STF-002|manager|manager123|manager@hallsymphony.com|0100000002|MANAGER|true|Management",

            // Schedulers
            "STF-003|scheduler1|sched123|scheduler1@hallsymphony.com|0100000003|SCHEDULER|true|Hall Operations",
            "STF-004|scheduler2|sched456|scheduler2@hallsymphony.com|0100000004|SCHEDULER|true|Hall Operations",

            // Customers: userId|username|password|email|phone|CUSTOMER|isActive|address|company
            "USR-001|john_doe|john123|john@email.com|0123456789|CUSTOMER|true|123 Main Street Kuala Lumpur|Acme Corp",
            "USR-002|jane_smith|jane123|jane@email.com|0187654321|CUSTOMER|true|456 Palm Avenue Petaling Jaya|TechSoft Sdn Bhd"
        };

        for (String line : defaultUsers) {
            FileHelper.appendLine("users.txt", line);
        }

        System.out.println("Seeded default users.");
    }

    private static void seedHalls() {
        File f = new File(FileHelper.DB_FOLDER + "halls.txt");
        if (f.exists()) return;  // Already seeded, skip

        // Format: hallId|hallName|hallType|capacity|ratePerHour|description
        String[] defaultHalls = {
            "HLL-001|Grand Auditorium|AUDITORIUM|1000|300.00|Spacious main auditorium for large conferences and events",
            "HLL-002|Crystal Banquet Hall|BANQUET_HALL|300|100.00|Elegant banquet hall ideal for weddings and dinners",
            "HLL-003|Meeting Room A|MEETING_ROOM|30|50.00|Cosy meeting room with projector and whiteboard",
            "HLL-004|Meeting Room B|MEETING_ROOM|30|50.00|Meeting room with video conferencing facilities"
        };

        for (String line : defaultHalls) {
            FileHelper.appendLine("halls.txt", line);
        }

        System.out.println("Seeded default halls.");
    }
}
