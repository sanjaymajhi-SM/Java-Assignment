package hallsymphony.ui.customer;

import hallsymphony.data.*;
import hallsymphony.model.*;
import hallsymphony.util.UIHelper;
import hallsymphony.ui.MainFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * CUSTOMER DASHBOARD
 * Tab 1 – Browse & Book Halls : view halls, check availability, book, pay, receipt
 * Tab 2 – My Bookings         : view, filter, cancel (3-day rule), view receipt
 * Tab 3 – Raise Issue         : submit issue, view manager response
 * Tab 4 – My Profile          : update personal info / password
 */
public class CustomerDashboard extends JFrame {

    private final Customer          me;
    private final HallFileManager   hallFM  = new HallFileManager();
    private final ScheduleFileManager schedFM = new ScheduleFileManager();
    private final BookingFileManager bookFM  = new BookingFileManager();
    private final IssueFileManager  issueFM = new IssueFileManager();

    private DefaultTableModel hallModel;
    private DefaultTableModel bookModel;
    private DefaultTableModel issueModel;
    private JComboBox<String>  bookCombo;

    public CustomerDashboard(Customer me) {
        this.me = me;
        setTitle("Customer Portal – " + me.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 740);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    /* ================================================================
       ROOT
    ================================================================ */
    private void buildUI() {
        setLayout(new BorderLayout());
        add(UIHelper.makeHeader("Customer Portal",
                        "Welcome, " + me.getUsername() + "   |   ID: " + me.getUserId()),
                BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("  Browse & Book Halls  ", buildHallTab());
        tabs.addTab("  My Bookings  ",         buildBookingsTab());
        tabs.addTab("  Raise an Issue  ",      buildIssueTab());
        tabs.addTab("  My Profile  ",          buildProfileTab());
        // Refresh issue tab booking combo whenever user switches to it
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 2) refreshBookCombo();
        });
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
       TAB 1 – BROWSE & BOOK
    ================================================================ */
    private JPanel buildHallTab() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIHelper.LIGHT_BG);

        // filter bar
        JTextField startF = UIHelper.makeField(17);
        JTextField endF   = UIHelper.makeField(17);
        startF.setText("2025-08-01 09:00");
        endF.setText("2025-08-01 13:00");
        startF.setToolTipText("yyyy-MM-dd HH:mm");
        endF.setToolTipText("yyyy-MM-dd HH:mm");

        JButton btnAvail   = UIHelper.secondaryBtn("Show Available");
        JButton btnAll     = UIHelper.secondaryBtn("Show All Halls");
        JButton btnBook    = UIHelper.primaryBtn("Book Selected Hall");

        JPanel toolbar = UIHelper.toolbar(
                UIHelper.boldLabel("From:"), startF,
                UIHelper.boldLabel("To:"), endF,
                btnAvail, btnAll,
                UIHelper.sep(),
                btnBook
        );
        root.add(toolbar, BorderLayout.NORTH);

        // table
        String[] cols = {"Hall ID","Hall Name","Type","Capacity","Rate/hr (RM)","Description"};
        hallModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(hallModel);
        UIHelper.styleTable(tbl);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(260);
        root.add(UIHelper.scrollPane(tbl), BorderLayout.CENTER);
        loadAllHalls();

        /* ---- actions ---- */
        btnAll.addActionListener(e -> loadAllHalls());

        btnAvail.addActionListener(e -> {
            String start = startF.getText().trim();
            String end   = endF.getText().trim();
            if (start.isEmpty() || end.isEmpty()) {
                UIHelper.err(this, "Enter a start and end time to filter availability."); return;
            }
            hallModel.setRowCount(0);
            for (Hall h : hallFM.getAllHalls()) {
                boolean ok = false;
                for (HallSchedule s : schedFM.getAvailabilityForHall(h.getHallId())) {
                    if (s.getStartDateTime().compareTo(start) <= 0
                            && s.getEndDateTime().compareTo(end) >= 0) { ok = true; break; }
                }
                if (ok) hallModel.addRow(new Object[]{
                        h.getHallId(), h.getHallName(), h.getHallType(),
                        h.getCapacity(), String.format("%.2f", h.getRatePerHour()), h.getDescription()
                });
            }
            if (hallModel.getRowCount() == 0)
                UIHelper.err(this, "No halls have availability scheduled for that time slot.\nScheduler must first add an availability window.");
        });

        btnBook.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UIHelper.err(this, "Select a hall row to book."); return; }
            Hall hall = hallFM.findById((String) hallModel.getValueAt(row, 0));
            if (hall != null) openBookingDialog(hall, startF.getText(), endF.getText());
        });

        return root;
    }

    private void loadAllHalls() {
        hallModel.setRowCount(0);
        for (Hall h : hallFM.getAllHalls()) {
            hallModel.addRow(new Object[]{
                    h.getHallId(), h.getHallName(), h.getHallType(),
                    h.getCapacity(), String.format("%.2f", h.getRatePerHour()), h.getDescription()
            });
        }
    }

    private void openBookingDialog(Hall hall, String defStart, String defEnd) {
        JDialog dlg = new JDialog(this, "Book Hall – " + hall.getHallName(), true);
        dlg.setSize(540, 450);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // header inside dialog
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UIHelper.DARK_BLUE);
        JLabel hlbl = new JLabel(hall.getHallName() + "  |  " + hall.getHallType()
                + "  |  RM " + String.format("%.2f", hall.getRatePerHour()) + "/hr");
        hlbl.setFont(new Font("Arial", Font.BOLD, 14));
        hlbl.setForeground(Color.WHITE);
        hdr.add(hlbl);
        dlg.add(hdr, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.LIGHT_BG);
        form.setBorder(BorderFactory.createEmptyBorder(16, 28, 10, 28));

        JTextField startF = UIHelper.makeField(24);
        JTextField endF   = UIHelper.makeField(24);
        startF.setText(defStart); endF.setText(defEnd);
        startF.setToolTipText("yyyy-MM-dd HH:mm");
        endF.setToolTipText("yyyy-MM-dd HH:mm");

        JTextArea eventA = new JTextArea(3, 26);
        eventA.setFont(new Font("Arial", Font.PLAIN, 13));
        eventA.setLineWrap(true);
        eventA.setBorder(BorderFactory.createLineBorder(new Color(180,200,230)));

        JComboBox<String> payCombo = new JComboBox<>(new String[]{
                "Cash","Credit / Debit Card","Online Transfer"});

        JLabel costLbl = new JLabel("  Estimated Cost: RM 0.00");
        costLbl.setFont(new Font("Arial", Font.BOLD, 14));
        costLbl.setForeground(UIHelper.DARK_BLUE);

        Runnable recalc = () -> {
            double c = BookingFileManager.calculateCost(
                    startF.getText().trim(), endF.getText().trim(), hall.getRatePerHour());
            costLbl.setText("  Estimated Cost: RM " + String.format("%.2f", c));
        };
        startF.addActionListener(ev -> recalc.run());
        endF.addActionListener(ev -> recalc.run());
        recalc.run();

        UIHelper.addRow(form, "Start (yyyy-MM-dd HH:mm) :", startF, 0);
        UIHelper.addRow(form, "End   (yyyy-MM-dd HH:mm) :", endF,   1);
        form.add(new JLabel(""), UIHelper.gbc(0,2));
        form.add(costLbl,        UIHelper.gbc(1,2));
        UIHelper.addRow(form, "Event Description :", new JScrollPane(eventA), 3);
        UIHelper.addRow(form, "Payment Method :",    payCombo, 4);

        JButton btnConfirm = UIHelper.successBtn("Confirm & Pay");
        JButton btnCancel  = UIHelper.secondaryBtn("Cancel");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnConfirm); btnRow.add(btnCancel);
        GridBagConstraints bc = UIHelper.gbcWide(5);
        bc.insets = new Insets(18, 8, 8, 8);
        form.add(btnRow, bc);

        dlg.add(form, BorderLayout.CENTER);
        btnCancel.addActionListener(ev -> dlg.dispose());

        btnConfirm.addActionListener(ev -> {
            String start  = startF.getText().trim();
            String end    = endF.getText().trim();
            String event  = eventA.getText().trim();
            String method = (String) payCombo.getSelectedItem();
            if (start.isEmpty()||end.isEmpty()) { UIHelper.err(dlg,"Enter start and end."); return; }
            if (event.isEmpty()) { UIHelper.err(dlg,"Enter event description."); return; }
            double cost = BookingFileManager.calculateCost(start, end, hall.getRatePerHour());
            if (cost <= 0) { UIHelper.err(dlg,"Invalid dates – end must be after start."); return; }

            if (!UIHelper.confirm(dlg, String.format(
                    "Confirm Booking?\n\nHall    : %s\nFrom    : %s\nTo      : %s\nEvent   : %s\n\nTotal   : RM %.2f\nPayment : %s",
                    hall.getHallName(), start, end, event, cost, method))) return;

            Booking b = new Booking(
                    bookFM.generateBookingId(), me.getUserId(), hall.getHallId(),
                    start, end, event, "CONFIRMED", cost, method, "PAID",
                    LocalDate.now().toString());
            bookFM.addBooking(b);
            refreshBookTable("All");
            dlg.dispose();
            showReceipt(b, hall);
        });

        dlg.setVisible(true);
    }

    private void showReceipt(Booking b, Hall hall) {
        JDialog r = new JDialog(this, "Payment Receipt – " + b.getBookingId(), true);
        r.setSize(460, 480);
        r.setLocationRelativeTo(this);
        r.setLayout(new BorderLayout(6,6));

        String txt =
                "==========================================\n"
                        + "         HALL SYMPHONY INC.\n"
                        + "           BOOKING RECEIPT\n"
                        + "==========================================\n"
                        + "Booking ID   : " + b.getBookingId()        + "\n"
                        + "Customer     : " + me.getUsername()         + "\n"
                        + "------------------------------------------\n"
                        + "Hall         : " + hall.getHallName()       + "\n"
                        + "Type         : " + hall.getHallType()       + "\n"
                        + "Event        : " + b.getEventDescription()  + "\n"
                        + "From         : " + b.getStartDateTime()     + "\n"
                        + "To           : " + b.getEndDateTime()       + "\n"
                        + "Rate /hr     : RM "+ String.format("%.2f", hall.getRatePerHour()) + "\n"
                        + "------------------------------------------\n"
                        + "TOTAL PAID   : RM " + String.format("%.2f", b.getTotalCost()) + "\n"
                        + "Payment      : " + b.getPaymentMethod()     + "\n"
                        + "Pay Status   : " + b.getPaymentStatus()     + "\n"
                        + "------------------------------------------\n"
                        + "Booked On    : " + b.getCreatedDate()       + "\n"
                        + "Status       : " + b.getStatus()            + "\n"
                        + "==========================================\n"
                        + "  Thank you for choosing Hall Symphony!\n"
                        + "==========================================";

        JTextArea area = new JTextArea(txt);
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(new Color(250,255,250));

        JButton close = UIHelper.primaryBtn("Close");
        close.addActionListener(ev -> r.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.add(close);

        r.add(new JScrollPane(area), BorderLayout.CENTER);
        r.add(bp, BorderLayout.SOUTH);
        r.setVisible(true);
    }

    /* ================================================================
       TAB 2 – MY BOOKINGS
    ================================================================ */
    private JPanel buildBookingsTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.LIGHT_BG);

        JComboBox<String> statusCombo = new JComboBox<>(
                new String[]{"All","CONFIRMED","COMPLETED","CANCELLED"});
        JButton btnFilter  = UIHelper.secondaryBtn("Filter");
        JButton btnShowAll = UIHelper.secondaryBtn("Show All");
        JButton btnCancel  = UIHelper.dangerBtn("Cancel Booking");
        JButton btnReceipt = UIHelper.secondaryBtn("View Receipt");

        root.add(UIHelper.toolbar(
                UIHelper.boldLabel("Status:"), statusCombo,
                btnFilter, btnShowAll,
                UIHelper.sep(),
                btnCancel, btnReceipt
        ), BorderLayout.NORTH);

        String[] cols = {"Booking ID","Hall","Start Date/Time","End Date/Time",
                "Total (RM)","Status","Payment","Event"};
        bookModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(bookModel);
        UIHelper.styleTable(tbl);
        tbl.getColumnModel().getColumn(7).setPreferredWidth(200);
        root.add(UIHelper.scrollPane(tbl), BorderLayout.CENTER);
        refreshBookTable("All");

        btnFilter.addActionListener(e  -> refreshBookTable((String)statusCombo.getSelectedItem()));
        btnShowAll.addActionListener(e -> { statusCombo.setSelectedIndex(0); refreshBookTable("All"); });
        statusCombo.addActionListener(e -> refreshBookTable((String)statusCombo.getSelectedItem()));

        btnCancel.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select a booking row to cancel."); return; }
            Booking b = bookFM.findById((String) bookModel.getValueAt(row, 0));
            if (b == null) return;
            if (!b.getStatus().equals("CONFIRMED")) {
                UIHelper.err(this,"Only CONFIRMED bookings can be cancelled."); return;
            }
            // 3-day rule
            try {
                LocalDate evDay  = LocalDate.parse(b.getStartDateTime().split(" ")[0]);
                long daysLeft    = ChronoUnit.DAYS.between(LocalDate.now(), evDay);
                if (daysLeft < 3) {
                    UIHelper.err(this,
                            "Cancellation not allowed!\n\n"
                                    + "Bookings must be cancelled at least 3 days before the event.\n"
                                    + "Your event is in " + daysLeft + " day(s) – too close to cancel.");
                    return;
                }
            } catch (Exception ignored) {}

            if (UIHelper.confirm(this,
                    "Cancel booking " + b.getBookingId() + "?\n\nA full refund will be issued.")) {
                b.setStatus("CANCELLED");
                b.setPaymentStatus("REFUNDED");
                bookFM.updateBooking(b);
                refreshBookTable((String)statusCombo.getSelectedItem());
                UIHelper.ok(this,"Booking cancelled. Refund has been processed.");
            }
        });

        btnReceipt.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select a booking to view."); return; }
            Booking b = bookFM.findById((String) bookModel.getValueAt(row, 0));
            if (b == null) return;
            Hall hall = hallFM.findById(b.getHallId());
            if (hall != null) showReceipt(b, hall);
        });

        return root;
    }

    private void refreshBookTable(String filter) {
        if (bookModel == null) return;
        bookModel.setRowCount(0);
        for (Booking b : bookFM.getByCustomer(me.getUserId())) {
            if (!filter.equals("All") && !b.getStatus().equals(filter)) continue;
            Hall h = hallFM.findById(b.getHallId());
            bookModel.addRow(new Object[]{
                    b.getBookingId(),
                    h != null ? h.getHallName() : b.getHallId(),
                    b.getStartDateTime(), b.getEndDateTime(),
                    String.format("%.2f", b.getTotalCost()),
                    b.getStatus(), b.getPaymentStatus(),
                    b.getEventDescription()
            });
        }
    }

    /* ================================================================
       TAB 3 – RAISE ISSUE
    ================================================================ */
    private JPanel buildIssueTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(UIHelper.LIGHT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        /* ---- submission form ---- */
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(UIHelper.LIGHT_BG);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIHelper.MID_BLUE),
                "  Submit a New Issue  "));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Show CONFIRMED and COMPLETED bookings (not cancelled)
        bookCombo = new JComboBox<>();
        for (Booking b : bookFM.getByCustomer(me.getUserId())) {
            String st = b.getStatus();
            if (st.equals("CONFIRMED") || st.equals("COMPLETED") || st.equals("PAID")) {
                Hall h = hallFM.findById(b.getHallId());
                bookCombo.addItem(b.getBookingId() + " | "
                        + (h != null ? h.getHallName() : b.getHallId())
                        + " | " + b.getStartDateTime()
                        + " [" + st + "]");
            }
        }

        JTextArea descA = new JTextArea(4, 30);
        descA.setFont(new Font("Arial", Font.PLAIN, 13));
        descA.setLineWrap(true);
        descA.setBorder(BorderFactory.createLineBorder(new Color(180,200,230)));

        UIHelper.addRow(form, "Select Booking :", bookCombo, 0);
        UIHelper.addRow(form, "Issue Description :", new JScrollPane(descA), 1);

        JButton btnSubmit = UIHelper.primaryBtn("Submit Issue");
        JPanel fbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        fbtn.setOpaque(false);
        fbtn.add(btnSubmit);
        GridBagConstraints bc = UIHelper.gbcWide(2);
        bc.insets = new Insets(10, 8, 8, 8);
        form.add(fbtn, bc);

        formPanel.add(form, BorderLayout.CENTER);
        root.add(formPanel, BorderLayout.NORTH);

        /* ---- issue history table ---- */
        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.setOpaque(false);
        JLabel lbl = UIHelper.boldLabel("  My Submitted Issues:");
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        tablePanel.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Issue ID","Booking ID","Hall","Description","Status","Manager Response","Date"};
        issueModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable issueTbl = new JTable(issueModel);
        UIHelper.styleTable(issueTbl);
        issueTbl.getColumnModel().getColumn(3).setPreferredWidth(200);
        issueTbl.getColumnModel().getColumn(5).setPreferredWidth(220);
        tablePanel.add(UIHelper.scrollPane(issueTbl), BorderLayout.CENTER);
        root.add(tablePanel, BorderLayout.CENTER);

        refreshIssueTable();

        /* ---- wire ---- */
        btnSubmit.addActionListener(e -> {
            if (bookCombo.getItemCount() == 0) {
                UIHelper.err(this, "You have no confirmed bookings to raise an issue against."); return;
            }
            String desc = descA.getText().trim();
            if (desc.isEmpty()) { UIHelper.err(this,"Please describe the issue."); return; }
            String bookingId = ((String)bookCombo.getSelectedItem()).split(" \\| ")[0];
            Issue iss = new Issue(issueFM.generateIssueId(), me.getUserId(), bookingId,
                    desc, "", "", "IN_PROGRESS", LocalDate.now().toString());
            issueFM.addIssue(iss);
            descA.setText("");
            refreshIssueTable();
            UIHelper.ok(this, "Issue submitted. A manager will respond soon.");
        });

        return root;
    }

    private void refreshIssueTable() {
        if (issueModel == null) return;
        issueModel.setRowCount(0);
        for (Issue i : issueFM.getByCustomer(me.getUserId())) {
            Booking b  = bookFM.findById(i.getBookingId());
            Hall    h  = (b != null) ? hallFM.findById(b.getHallId()) : null;
            String desc = i.getDescription().length() > 45
                    ? i.getDescription().substring(0,45) + "…" : i.getDescription();
            issueModel.addRow(new Object[]{
                    i.getIssueId(), i.getBookingId(),
                    h != null ? h.getHallName() : "—",
                    desc, i.getStatus(),
                    i.getManagerResponse().isEmpty() ? "(Pending response)" : i.getManagerResponse(),
                    i.getCreatedDate()
            });
        }
    }

    /* ================================================================
       TAB 4 – MY PROFILE
    ================================================================ */
    private JPanel buildProfileTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UIHelper.LIGHT_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.LIGHT_BG);
        form.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIHelper.MID_BLUE), "  Update My Profile  "));
        form.setPreferredSize(new Dimension(480, 360));

        JTextField     emailF   = UIHelper.makeField(26); emailF.setText(me.getEmail());
        JTextField     phoneF   = UIHelper.makeField(26); phoneF.setText(me.getPhone());
        JTextField     addrF    = UIHelper.makeField(26); addrF.setText(me.getAddress());
        JPasswordField passF    = UIHelper.makePasswordField(26);
        JPasswordField confF    = UIHelper.makePasswordField(26);

        UIHelper.addRow(form, "Email :",            emailF, 0);
        UIHelper.addRow(form, "Phone :",            phoneF, 1);
        UIHelper.addRow(form, "Address :",          addrF,  2);
        UIHelper.addRow(form, "New Password :",     passF,  3);
        UIHelper.addRow(form, "Confirm Password :", confF,  4);

        JButton btnSave = UIHelper.primaryBtn("Save Changes");
        JPanel fbtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        fbtn.setOpaque(false);
        fbtn.add(btnSave);
        GridBagConstraints bc = UIHelper.gbcWide(5);
        bc.insets = new Insets(18, 8, 8, 8);
        form.add(fbtn, bc);

        outer.add(form);

        btnSave.addActionListener(e -> {
            me.setEmail(emailF.getText().trim());
            me.setPhone(phoneF.getText().trim());
            me.setAddress(addrF.getText().trim());
            String np = new String(passF.getPassword()).trim();
            String cp = new String(confF.getPassword()).trim();
            if (!np.isEmpty()) {
                if (np.length() < 6) { UIHelper.err(this,"Password must be at least 6 characters."); return; }
                if (!np.equals(cp))  { UIHelper.err(this,"Passwords do not match."); return; }
                me.setPassword(np);
            }
            new UserFileManager().updateUser(me);
            UIHelper.ok(this,"Profile updated successfully.");
        });

        return outer;
    }

    private void refreshBookCombo() {
        if (bookCombo == null) return;
        bookCombo.removeAllItems();
        for (Booking b : bookFM.getByCustomer(me.getUserId())) {
            String st = b.getStatus();
            if (st.equals("CONFIRMED") || st.equals("COMPLETED") || st.equals("PAID")) {
                Hall h = hallFM.findById(b.getHallId());
                bookCombo.addItem(b.getBookingId() + " | "
                        + (h != null ? h.getHallName() : b.getHallId())
                        + " | " + b.getStartDateTime()
                        + " [" + st + "]");
            }
        }
    }
}