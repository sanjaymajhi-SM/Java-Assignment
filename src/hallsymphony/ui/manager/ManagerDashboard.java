package hallsymphony.ui.manager;

import hallsymphony.data.*;
import hallsymphony.model.*;
import hallsymphony.util.UIHelper;
import hallsymphony.ui.MainFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * MANAGER DASHBOARD
 * Tab 1 – Sales Dashboard   : revenue cards (week/month/year), filter bookings
 * Tab 2 – Maintenance Issues: view, respond, assign scheduler, change status
 */
public class ManagerDashboard extends JFrame {

    private final Staff              me;
    private final BookingFileManager bookFM  = new BookingFileManager();
    private final IssueFileManager   issueFM = new IssueFileManager();
    private final HallFileManager    hallFM  = new HallFileManager();
    private final UserFileManager    userFM  = new UserFileManager();

    // Sales tab
    private DefaultTableModel salesModel;
    private JLabel weekLbl, monthLbl, yearLbl, totalLbl, filteredLbl;

    // Issues tab
    private DefaultTableModel issueModel;
    private JTable            issueTable;

    public ManagerDashboard(Staff me) {
        this.me = me;
        setTitle("Manager Dashboard – " + me.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1160, 760);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    /* ================================================================
       ROOT
    ================================================================ */
    private void buildUI() {
        setLayout(new BorderLayout());
        add(UIHelper.makeHeader("Manager Dashboard",
            "Logged in as: " + me.getUsername() + "   |   Role: MANAGER"),
            BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("  Sales Dashboard  ",    buildSalesTab());
        tabs.addTab("  Maintenance & Issues  ", buildIssueTab());
        add(tabs, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        bar.setBackground(new Color(230, 235, 245));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 200, 230)));
        JButton logout = UIHelper.dangerBtn("  Logout  ");
        logout.addActionListener(e -> { dispose(); MainFrame.logout(); });
        bar.add(logout);
        add(bar, BorderLayout.SOUTH);
    }

    /* ================================================================
       TAB 1 – SALES DASHBOARD
    ================================================================ */
    private JPanel buildSalesTab() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIHelper.LIGHT_BG);

        /* ---- summary cards ---- */
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setBackground(new Color(220, 230, 248));
        cards.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        weekLbl   = addCard(cards, "This Week",          new Color(41, 98, 162));
        monthLbl  = addCard(cards, "This Month",         new Color(27, 60, 115));
        yearLbl   = addCard(cards, "This Year",          new Color(30, 120, 30));
        totalLbl  = addCard(cards, "Total Bookings",     new Color(150, 75, 0));
        root.add(cards, BorderLayout.NORTH);

        /* ---- filter toolbar ---- */
        JComboBox<String> periodC = new JComboBox<>(
            new String[]{"All Time","This Week","This Month","This Year"});
        JTextField hallF = UIHelper.makeField(16);
        hallF.setToolTipText("Filter by hall name");
        JButton btnApply = UIHelper.primaryBtn("Apply Filter");
        JButton btnReset = UIHelper.secondaryBtn("Reset");

        filteredLbl = new JLabel("  Showing all bookings");
        filteredLbl.setFont(new Font("Arial", Font.ITALIC, 12));
        filteredLbl.setForeground(UIHelper.MID_BLUE);

        JPanel toolbar = UIHelper.toolbar(
            UIHelper.boldLabel("Period:"), periodC,
            UIHelper.boldLabel("  Hall:"), hallF,
            btnApply, btnReset,
            filteredLbl
        );
        root.add(toolbar, BorderLayout.CENTER);   // will be displaced by CENTER table below

        /* ---- sales table ---- */
        String[] cols = {"Booking ID","Customer","Hall","Type","Start","End",
                         "Total (RM)","Method","Pay Status","Booking Status"};
        salesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(salesModel);
        UIHelper.styleTable(tbl);

        // Lay out correctly: cards NORTH, toolbar + table CENTER
        JPanel mid = new JPanel(new BorderLayout(0, 0));
        mid.setOpaque(false);
        mid.add(toolbar, BorderLayout.NORTH);
        mid.add(UIHelper.scrollPane(tbl), BorderLayout.CENTER);
        root.remove(toolbar);   // remove from previous add
        root.add(mid, BorderLayout.CENTER);

        Runnable load = () -> {
            String period = (String) periodC.getSelectedItem();
            String hallQ  = hallF.getText().trim().toLowerCase();
            salesModel.setRowCount(0);
            double revenue = 0;
            LocalDate today = LocalDate.now();

            for (Booking b : bookFM.getAllBookings()) {
                if (b.getStatus().equals("CANCELLED")) continue;
                try {
                    LocalDate bd = LocalDate.parse(b.getCreatedDate());
                    boolean in = switch (period) {
                        case "This Week"  -> !bd.isBefore(today.minusDays(today.getDayOfWeek().getValue()-1));
                        case "This Month" -> bd.getMonthValue()==today.getMonthValue() && bd.getYear()==today.getYear();
                        case "This Year"  -> bd.getYear()==today.getYear();
                        default -> true;
                    };
                    if (!in) continue;
                } catch (Exception ignored) {}

                Hall  h = hallFM.findById(b.getHallId());
                User  u = userFM.findById(b.getCustomerId());
                String hname = h != null ? h.getHallName() : b.getHallId();
                if (!hallQ.isEmpty() && !hname.toLowerCase().contains(hallQ)) continue;

                revenue += b.getTotalCost();
                salesModel.addRow(new Object[]{
                    b.getBookingId(),
                    u != null ? u.getUsername() : b.getCustomerId(),
                    hname,
                    h != null ? h.getHallType() : "—",
                    b.getStartDateTime(), b.getEndDateTime(),
                    String.format("%.2f", b.getTotalCost()),
                    b.getPaymentMethod(), b.getPaymentStatus(), b.getStatus()
                });
            }

            filteredLbl.setText(String.format(
                "  Filtered Revenue: RM %.2f   |   Rows: %d",
                revenue, salesModel.getRowCount()));
            updateCards(today);
        };

        load.run();
        btnApply.addActionListener(e -> load.run());
        periodC.addActionListener(e -> load.run());
        btnReset.addActionListener(e -> { periodC.setSelectedIndex(0); hallF.setText(""); load.run(); });

        return root;
    }

    /** Coloured summary card — returns the value JLabel for live updates */
    private JLabel addCard(JPanel parent, String title, Color color) {
        JPanel card = new JPanel(new GridLayout(2,1,0,4));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(12,16,12,16));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 12));
        t.setForeground(new Color(200,225,255));
        JLabel v = new JLabel("RM 0.00");
        v.setFont(new Font("Arial", Font.BOLD, 20));
        v.setForeground(Color.WHITE);
        card.add(t); card.add(v);
        parent.add(card);
        return v;
    }

    private void updateCards(LocalDate today) {
        weekLbl.setText ("RM " + String.format("%.2f", calcRev("This Week",  today)));
        monthLbl.setText("RM " + String.format("%.2f", calcRev("This Month", today)));
        yearLbl.setText ("RM " + String.format("%.2f", calcRev("This Year",  today)));
        long tot = bookFM.getAllBookings().stream().filter(b->!b.getStatus().equals("CANCELLED")).count();
        totalLbl.setText(String.valueOf(tot) + " bookings");
    }

    private double calcRev(String period, LocalDate today) {
        double sum = 0;
        for (Booking b : bookFM.getAllBookings()) {
            if (b.getStatus().equals("CANCELLED")) continue;
            try {
                LocalDate bd = LocalDate.parse(b.getCreatedDate());
                boolean in = switch (period) {
                    case "This Week"  -> !bd.isBefore(today.minusDays(today.getDayOfWeek().getValue()-1));
                    case "This Month" -> bd.getMonthValue()==today.getMonthValue() && bd.getYear()==today.getYear();
                    case "This Year"  -> bd.getYear()==today.getYear();
                    default -> true;
                };
                if (in) sum += b.getTotalCost();
            } catch (Exception ignored) {}
        }
        return sum;
    }

    /* ================================================================
       TAB 2 – MAINTENANCE & ISSUES
    ================================================================ */
    private JPanel buildIssueTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.LIGHT_BG);

        JComboBox<String> statusC = new JComboBox<>(
            new String[]{"All","IN_PROGRESS","DONE","CLOSED","CANCELLED"});

        JButton btnFilter   = UIHelper.secondaryBtn("Filter");
        JButton btnAll      = UIHelper.secondaryBtn("Show All");
        JButton btnRespond  = UIHelper.primaryBtn("Respond to Issue");
        JButton btnAssign   = UIHelper.successBtn("Assign Scheduler");
        JButton btnStatus   = UIHelper.warnBtn("Change Status");

        root.add(UIHelper.toolbar(
            UIHelper.boldLabel("Status:"), statusC,
            btnFilter, btnAll,
            UIHelper.sep(),
            btnRespond, btnAssign, btnStatus
        ), BorderLayout.NORTH);

        String[] cols = {"Issue ID","Customer","Booking ID","Hall",
                         "Description","Assigned To","Status","Manager Response","Date"};
        issueModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        issueTable = new JTable(issueModel);
        UIHelper.styleTable(issueTable);
        issueTable.getColumnModel().getColumn(4).setPreferredWidth(210);
        issueTable.getColumnModel().getColumn(7).setPreferredWidth(210);
        root.add(UIHelper.scrollPane(issueTable), BorderLayout.CENTER);
        refreshIssueTable("All");

        btnFilter.addActionListener(e  -> refreshIssueTable((String)statusC.getSelectedItem()));
        btnAll.addActionListener(e    -> { statusC.setSelectedIndex(0); refreshIssueTable("All"); });

        /* ---- Respond ---- */
        btnRespond.addActionListener(e -> {
            int row = issueTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select an issue row."); return; }
            Issue iss = issueFM.findById((String) issueModel.getValueAt(row, 0));
            if (iss == null) return;

            String cur = iss.getManagerResponse().isEmpty() ? "" : iss.getManagerResponse();
            String resp = (String) JOptionPane.showInputDialog(
                this, "Enter your response to the customer:",
                "Respond to Issue – " + iss.getIssueId(),
                JOptionPane.PLAIN_MESSAGE, null, null, cur);

            if (resp != null && !resp.trim().isEmpty()) {
                iss.setManagerResponse(resp.trim());
                issueFM.updateIssue(iss);
                refreshIssueTable((String)statusC.getSelectedItem());
                UIHelper.ok(this,"Response saved. Customer can now see it.");
            }
        });

        /* ---- Assign Scheduler ---- */
        btnAssign.addActionListener(e -> {
            int row = issueTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select an issue row."); return; }
            Issue iss = issueFM.findById((String) issueModel.getValueAt(row, 0));
            if (iss == null) return;

            List<User> schedulers = userFM.getByRole("SCHEDULER");
            if (schedulers.isEmpty()) {
                UIHelper.err(this,"No scheduler accounts exist. Ask the Admin to add one."); return;
            }
            String[] opts = new String[schedulers.size()];
            for (int i = 0; i < schedulers.size(); i++)
                opts[i] = schedulers.get(i).getUserId() + " — " + schedulers.get(i).getUsername();

            String chosen = (String) JOptionPane.showInputDialog(
                this, "Assign issue " + iss.getIssueId() + " to a scheduler:",
                "Assign Scheduler", JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);

            if (chosen != null) {
                String schedId   = chosen.split(" — ")[0];
                String schedName = chosen.split(" — ")[1];
                iss.setAssignedSchedulerId(schedId);
                iss.setStatus("IN_PROGRESS");
                issueFM.updateIssue(iss);
                refreshIssueTable((String)statusC.getSelectedItem());
                UIHelper.ok(this,"Issue assigned to scheduler: " + schedName);
            }
        });

        /* ---- Change Status ---- */
        btnStatus.addActionListener(e -> {
            int row = issueTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select an issue row."); return; }
            Issue iss = issueFM.findById((String) issueModel.getValueAt(row, 0));
            if (iss == null) return;

            String[] opts = {"IN_PROGRESS","DONE","CLOSED","CANCELLED"};
            String chosen = (String) JOptionPane.showInputDialog(
                this, "Select new status for issue " + iss.getIssueId() + ":",
                "Change Issue Status", JOptionPane.PLAIN_MESSAGE,
                null, opts, iss.getStatus());

            if (chosen != null) {
                iss.setStatus(chosen);
                issueFM.updateIssue(iss);
                refreshIssueTable((String)statusC.getSelectedItem());
                UIHelper.ok(this,"Issue status updated to: " + chosen);
            }
        });

        return root;
    }

    private void refreshIssueTable(String filter) {
        issueModel.setRowCount(0);
        for (Issue i : issueFM.getAllIssues()) {
            if (!filter.equals("All") && !i.getStatus().equals(filter)) continue;
            User  cust  = userFM.findById(i.getCustomerId());
            User  sched = (i.getAssignedSchedulerId() != null && !i.getAssignedSchedulerId().isEmpty())
                          ? userFM.findById(i.getAssignedSchedulerId()) : null;
            Booking b   = (i.getBookingId() != null && !i.getBookingId().isEmpty())
                          ? bookFM.findById(i.getBookingId()) : null;
            Hall    h   = (b != null) ? hallFM.findById(b.getHallId()) : null;

            String desc = i.getDescription().length() > 50
                ? i.getDescription().substring(0,50) + "…" : i.getDescription();

            issueModel.addRow(new Object[]{
                i.getIssueId(),
                cust  != null ? cust.getUsername()  : i.getCustomerId(),
                i.getBookingId(),
                h     != null ? h.getHallName()     : "—",
                desc,
                sched != null ? sched.getUsername() : "Unassigned",
                i.getStatus(),
                i.getManagerResponse().isEmpty() ? "—" : i.getManagerResponse(),
                i.getCreatedDate()
            });
        }
    }
}
