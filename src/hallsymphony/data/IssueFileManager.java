package hallsymphony.data;

import hallsymphony.model.Issue;
import java.util.ArrayList;
import java.util.List;

/**
 * IssueFileManager - reads and writes issues.txt
 *
 * FILE FORMAT (issues.txt):
 *   issueId|customerId|bookingId|description|managerResponse|assignedSchedulerId|status|createdDate
 *
 * Example:
 *   ISS-001|USR-001|BKG-001|AC not working|We will fix it|STF-003|IN_PROGRESS|2025-07-10
 */
public class IssueFileManager {

    private static final String FILE = "issues.txt";

    // ── Read ──────────────────────────────────────────────────────────────

    public List<Issue> getAllIssues() {
        List<Issue> list = new ArrayList<>();

        for (String line : FileHelper.readAllLines(FILE)) {
            Issue issue = parseLine(line);
            if (issue != null) list.add(issue);
        }

        return list;
    }

    private Issue parseLine(String line) {
        String[] f = FileHelper.splitLine(line);
        if (f.length < 8) return null;

        return new Issue(
            FileHelper.getField(f, 0),   // issueId
            FileHelper.getField(f, 1),   // customerId
            FileHelper.getField(f, 2),   // bookingId
            FileHelper.getField(f, 3),   // description
            FileHelper.getField(f, 4),   // managerResponse
            FileHelper.getField(f, 5),   // assignedSchedulerId
            FileHelper.getField(f, 6),   // status
            FileHelper.getField(f, 7)    // createdDate
        );
    }

    public Issue findById(String issueId) {
        for (Issue i : getAllIssues()) {
            if (i.getIssueId().equals(issueId)) return i;
        }
        return null;
    }

    public List<Issue> getByCustomer(String customerId) {
        List<Issue> result = new ArrayList<>();
        for (Issue i : getAllIssues()) {
            if (i.getCustomerId().equals(customerId)) result.add(i);
        }
        return result;
    }

    // ── Add ───────────────────────────────────────────────────────────────

    public void addIssue(Issue issue) {
        FileHelper.appendLine(FILE, buildLine(issue));
    }

    // ── Update ────────────────────────────────────────────────────────────

    public void updateIssue(Issue updated) {
        List<String> lines    = FileHelper.readAllLines(FILE);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String[] f = FileHelper.splitLine(line);
            if (FileHelper.getField(f, 0).equals(updated.getIssueId())) {
                newLines.add(buildLine(updated));
            } else {
                newLines.add(line);
            }
        }

        FileHelper.writeAllLines(FILE, newLines);
    }

    // ── Build line ────────────────────────────────────────────────────────

    private String buildLine(Issue i) {
        return FileHelper.joinFields(
            i.getIssueId(),
            i.getCustomerId(),
            i.getBookingId(),
            i.getDescription(),
            i.getManagerResponse()     != null ? i.getManagerResponse()     : "",
            i.getAssignedSchedulerId() != null ? i.getAssignedSchedulerId() : "",
            i.getStatus(),
            i.getCreatedDate()
        );
    }

    // ── Generate ID ───────────────────────────────────────────────────────

    public String generateIssueId() {
        int next = getAllIssues().size() + 1;
        return "ISS-" + String.format("%03d", next);
    }
}
