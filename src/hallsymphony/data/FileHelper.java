package hallsymphony.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHelper - handles all low-level reading and writing of TXT files.
 *
 * Each TXT file stores one record per line.
 * Fields within a line are separated by the pipe "|" character.
 *
 * Example line in users.txt:
 *   USR-001|john_doe|password123|john@email.com|0123456789|CUSTOMER|true|123 Main St|Acme Corp
 *
 * This class is used by all FileManager classes to read and write TXT files.
 */
public class FileHelper {

    // The folder where all .txt database files are stored
    public static final String DB_FOLDER = "database/";

    // The separator used between fields in each line
    public static final String SEPARATOR = "|";

    /**
     * Reads all lines from a TXT file.
     * Skips blank lines and comment lines (starting with #).
     */
    public static List<String> readAllLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(DB_FOLDER + filename);

        // If file doesn't exist yet, return empty list
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + filename + " | " + e.getMessage());
        }

        return lines;
    }

    /**
     * Writes a list of lines to a TXT file, completely replacing its contents.
     */
    public static void writeAllLines(String filename, List<String> lines) {
        // Make sure the database folder exists
        new File(DB_FOLDER).mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DB_FOLDER + filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + filename + " | " + e.getMessage());
        }
    }

    /**
     * Appends a single new line to the end of a TXT file.
     * Used when adding a new record.
     */
    public static void appendLine(String filename, String line) {
        new File(DB_FOLDER).mkdirs();

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(DB_FOLDER + filename, true))) {   // true = append mode
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error appending to file: " + filename + " | " + e.getMessage());
        }
    }

    /**
     * Splits a line into fields using the "|" separator.
     * Returns a String array of field values.
     */
    public static String[] splitLine(String line) {
        // -1 keeps trailing empty strings (important for optional fields)
        return line.split("\\|", -1);
    }

    /**
     * Joins fields into a single line using the "|" separator.
     */
    public static String joinFields(String... fields) {
        return String.join(SEPARATOR, fields);
    }

    /**
     * Safely gets a field value at a given index.
     * Returns empty string if index is out of bounds.
     */
    public static String getField(String[] fields, int index) {
        if (index < fields.length) {
            return fields[index].trim();
        }
        return "";
    }
}
