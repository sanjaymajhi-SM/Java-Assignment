package hallsymphony.ui.admin;

import hallsymphony.data.*;
import hallsymphony.model.*;
import hallsymphony.util.UIHelper;
import hallsymphony.ui.MainFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * ADMIN DASHBOARD
 * Tab 1 – Scheduler Staff  : Add / View+Filter / Edit / Delete scheduler accounts
 * Tab 2 – User Management  : View+Filter / Block-Unblock / Delete customer accounts
 * Tab 3 – All Bookings     : View+Filter upcoming & past bookings for all customers
 */
public class AdminDashboard extends JFrame {

    private final Staff              me;
    private final UserFileManager    userFM = new UserFileManager();
    private final HallFileManager    hallFM = new HallFileManager();
    private final BookingFileManager bookFM = new BookingFileManager();

    private DefaultTableModel schedModel;
    private DefaultTableModel userModel;
    private DefaultTableModel bookModel;

    private JTable schedTable;
    private JTable userTable;
    private JTable bookTable;

    public AdminDashboard(Staff me) {
        this.me = me;
        setTitle("Admin Dashboard – " + me.getUsername());
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
        add(UIHelper.makeHeader("Administrator Dashboard",
                        "Logged in as: " + me.getUsername() + "   |   Role: ADMIN"),
                BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("  Scheduler Staff  ",  buildSchedulerTab());
        tabs.addTab("  User Management  ",  buildUserTab());
        tabs.addTab("  All Bookings  ",     buildBookingsTab());
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
       TAB 1 – SCHEDULER STAFF MANAGEMENT
    ================================================================ */
    private JPanel buildSchedulerTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.LIGHT_BG);

        JTextField filterF = UIHelper.makeField(20);
        filterF.setToolTipText("Filter by username or email");

        JButton btnSearch  = UIHelper.secondaryBtn("Search");
        JButton btnAll     = UIHelper.secondaryBtn("Show All");
        JButton btnAdd     = UIHelper.primaryBtn("+ Add Scheduler");
        JButton btnEdit    = UIHelper.warnBtn("Edit Selected");
        JButton btnDelete  = UIHelper.dangerBtn("Delete Selected");

        root.add(UIHelper.toolbar(
                UIHelper.boldLabel("Filter:"), filterF,
                btnSearch, btnAll,
                UIHelper.sep(),
                btnAdd, btnEdit, btnDelete
        ), BorderLayout.NORTH);

        String[] cols = {"Staff ID","Username","Email","Phone","Department","Active"};
        schedModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        schedTable = new JTable(schedModel);
        UIHelper.styleTable(schedTable);
        root.add(UIHelper.scrollPane(schedTable), BorderLayout.CENTER);
        refreshSchedTable("");

        btnSearch.addActionListener(e  -> refreshSchedTable(filterF.getText().trim()));
        filterF.addActionListener(e   -> refreshSchedTable(filterF.getText().trim()));
        btnAll.addActionListener(e    -> { filterF.setText(""); refreshSchedTable(""); });

        btnAdd.addActionListener(e -> openSchedDialog(null));

        btnEdit.addActionListener(e -> {
            int row = schedTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select a row to edit."); return; }
            Staff s = (Staff) userFM.findById((String) schedModel.getValueAt(row, 0));
            if (s != null) openSchedDialog(s);
        });

        btnDelete.addActionListener(e -> {
            int row = schedTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select a row to delete."); return; }
            String id   = (String) schedModel.getValueAt(row, 0);
            String name = (String) schedModel.getValueAt(row, 1);
            if (UIHelper.confirm(this,"Delete scheduler account \"" + name + "\"?")) {
                userFM.deleteUser(id);
                refreshSchedTable("");
                UIHelper.ok(this,"Scheduler deleted.");
            }
        });

        return root;
    }

    private void refreshSchedTable(String filter) {
        schedModel.setRowCount(0);
        String f = filter.toLowerCase();
        for (User u : userFM.getByRole("SCHEDULER")) {
            Staff s = (Staff) u;
            if (f.isEmpty() || s.getUsername().toLowerCase().contains(f)
                    || s.getEmail().toLowerCase().contains(f)) {
                schedModel.addRow(new Object[]{
                        s.getUserId(), s.getUsername(), s.getEmail(),
                        s.getPhone(), s.getDepartment(),
                        s.isActive() ? "Yes" : "No"
                });
            }
        }
    }

    private void openSchedDialog(Staff existing) {
        boolean edit = (existing != null);
        JDialog dlg = new JDialog(this, edit ? "Edit Scheduler" : "Add New Scheduler", true);
        dlg.setSize(480, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.LIGHT_BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JTextField     userF  = UIHelper.makeField(26);
        JTextField     emailF = UIHelper.makeField(26);
        JTextField     phoneF = UIHelper.makeField(26);
        JTextField     deptF  = UIHelper.makeField(26);
        JPasswordField passF  = UIHelper.makePasswordField(26);

        if (edit) {
            userF.setText(existing.getUsername());
            userF.setEnabled(false);
            emailF.setText(existing.getEmail());
            phoneF.setText(existing.getPhone());
            deptF.setText(existing.getDepartment());
        } else {
            deptF.setText("Hall Operations");
        }

        UIHelper.addRow(form, "Username :", userF, 0);
        UIHelper.addRow(form, "Email :",    emailF, 1);
        UIHelper.addRow(form, "Phone :",    phoneF, 2);
        UIHelper.addRow(form, "Department :", deptF, 3);

        JLabel pLbl = UIHelper.boldLabel(edit ? "New Password (blank = keep):" : "Password :");
        form.add(pLbl,  UIHelper.gbc(0, 4));
        form.add(passF, UIHelper.gbc(1, 4));

        JButton btnSave   = UIHelper.primaryBtn(edit ? "Save Changes" : "Add Scheduler");
        JButton btnCancel = UIHelper.secondaryBtn("Cancel");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSave); btnRow.add(btnCancel);
        GridBagConstraints bc = UIHelper.gbcWide(5);
        bc.insets = new Insets(18, 8, 8, 8);
        form.add(btnRow, bc);

        dlg.add(form, BorderLayout.CENTER);
        btnCancel.addActionListener(ev -> dlg.dispose());

        btnSave.addActionListener(ev -> {
            String uname = userF.getText().trim();
            String email = emailF.getText().trim();
            String phone = phoneF.getText().trim();
            String dept  = deptF.getText().trim();
            String pass  = new String(passF.getPassword());

            if (uname.isEmpty() || email.isEmpty()) {
                UIHelper.err(dlg,"Username and email are required."); return;
            }
            if (edit) {
                existing.setEmail(email);
                existing.setPhone(phone);
                existing.setDepartment(dept);
                if (!pass.isEmpty()) {
                    if (pass.length() < 6) { UIHelper.err(dlg,"Password must be >= 6 chars."); return; }
                    existing.setPassword(pass);
                }
                userFM.updateUser(existing);
                UIHelper.ok(dlg,"Scheduler info updated.");
            } else {
                if (pass.length() < 6) { UIHelper.err(dlg,"Password must be >= 6 chars."); return; }
                if (userFM.usernameExists(uname)) { UIHelper.err(dlg,"Username already exists."); return; }
                Staff ns = new Staff(userFM.generateUserId("SCHEDULER"),
                        uname, pass, email, phone, "SCHEDULER", dept, true);
                userFM.addUser(ns);
                UIHelper.ok(dlg,"Scheduler \"" + uname + "\" added.");
            }
            refreshSchedTable("");
            dlg.dispose();
        });

        dlg.setVisible(true);
    }

    /* ================================================================
       TAB 2 – USER (CUSTOMER) MANAGEMENT
    ================================================================ */
    private JPanel buildUserTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.LIGHT_BG);

        JTextField filterF = UIHelper.makeField(20);
        filterF.setToolTipText("Filter by username or email");

        JButton btnSearch  = UIHelper.secondaryBtn("Search");
        JButton btnAll     = UIHelper.secondaryBtn("Show All");
        JButton btnBlock   = UIHelper.warnBtn("Block / Unblock");
        JButton btnDelete  = UIHelper.dangerBtn("Delete User");

        root.add(UIHelper.toolbar(
                UIHelper.boldLabel("Filter:"), filterF,
                btnSearch, btnAll,
                UIHelper.sep(),
                btnBlock, btnDelete
        ), BorderLayout.NORTH);

        String[] cols = {"User ID","Username","Email","Phone","Status"};
        userModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userModel);
        UIHelper.styleTable(userTable);
        root.add(UIHelper.scrollPane(userTable), BorderLayout.CENTER);
        refreshUserTable("");

        btnSearch.addActionListener(e  -> refreshUserTable(filterF.getText().trim()));
        filterF.addActionListener(e   -> refreshUserTable(filterF.getText().trim()));
        btnAll.addActionListener(e    -> { filterF.setText(""); refreshUserTable(""); });

        btnBlock.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select a user row."); return; }
            String id   = (String) userModel.getValueAt(row, 0);
            String name = (String) userModel.getValueAt(row, 1);
            User u = userFM.findById(id);
            if (u == null) return;
            boolean nowActive = !u.isActive();
            u.setActive(nowActive);
            userFM.updateUser(u);
            refreshUserTable(filterF.getText().trim());
            UIHelper.ok(this,"User \"" + name + "\" is now " + (nowActive ? "ACTIVE." : "BLOCKED."));
        });

        btnDelete.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this,"Select a user row."); return; }
            String id   = (String) userModel.getValueAt(row, 0);
            String name = (String) userModel.getValueAt(row, 1);
            if (UIHelper.confirm(this,"Permanently delete user \"" + name + "\"?\nThis cannot be undone.")) {
                userFM.deleteUser(id);
                refreshUserTable("");
            }
        });

        return root;
    }

    private void refreshUserTable(String filter) {
        userModel.setRowCount(0);
        String f = filter.toLowerCase();
        for (User u : userFM.getByRole("CUSTOMER")) {
            Customer c = (Customer) u;
            if (f.isEmpty() || c.getUsername().toLowerCase().contains(f)
                    || c.getEmail().toLowerCase().contains(f)) {
                userModel.addRow(new Object[]{
                        c.getUserId(), c.getUsername(), c.getEmail(),
                        c.getPhone(),
                        c.isActive() ? "Active" : "BLOCKED"
                });
            }
        }
    }

    /* ================================================================
       TAB 3 – ALL BOOKINGS
    ================================================================ */
    private JPanel buildBookingsTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.LIGHT_BG);

        JTextField searchF = UIHelper.makeField(18);
        searchF.setToolTipText("Search by customer name or booking ID");
        JComboBox<String> statusC = new JComboBox<>(
                new String[]{"All","Upcoming (CONFIRMED)","Past (COMPLETED)","CANCELLED"});

        JButton btnFilter = UIHelper.secondaryBtn("Filter");
        JButton btnAll    = UIHelper.secondaryBtn("Show All");

        root.add(UIHelper.toolbar(
                UIHelper.boldLabel("Search:"), searchF,
                UIHelper.boldLabel("  Status:"), statusC,
                btnFilter, btnAll
        ), BorderLayout.NORTH);

        String[] cols = {"Booking ID","Customer","Hall","Start","End","Total (RM)","Payment","Status"};
        bookModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookTable = new JTable(bookModel);
        UIHelper.styleTable(bookTable);
        root.add(UIHelper.scrollPane(bookTable), BorderLayout.CENTER);
        refreshBookTable("All","");

        btnFilter.addActionListener(e ->
                refreshBookTable((String)statusC.getSelectedItem(), searchF.getText().trim()));
        btnAll.addActionListener(e -> {
            searchF.setText(""); statusC.setSelectedIndex(0); refreshBookTable("All","");
        });

        return root;
    }

    private void refreshBookTable(String statusF, String search) {
        if (bookModel == null) return;
        bookModel.setRowCount(0);
        String s = search.toLowerCase();
        for (Booking b : bookFM.getAllBookings()) {
            boolean ok = switch (statusF) {
                case "Upcoming (CONFIRMED)" -> b.getStatus().equals("CONFIRMED");
                case "Past (COMPLETED)"     -> b.getStatus().equals("COMPLETED");
                case "CANCELLED"            -> b.getStatus().equals("CANCELLED");
                default -> true;
            };
            if (!ok) continue;
            User  u = userFM.findById(b.getCustomerId());
            Hall  h = hallFM.findById(b.getHallId());
            String cname = u != null ? u.getUsername() : b.getCustomerId();
            String hname = h != null ? h.getHallName() : b.getHallId();
            if (!s.isEmpty() && !cname.toLowerCase().contains(s)
                    && !b.getBookingId().toLowerCase().contains(s)) continue;
            bookModel.addRow(new Object[]{
                    b.getBookingId(), cname, hname,
                    b.getStartDateTime(), b.getEndDateTime(),
                    String.format("%.2f", b.getTotalCost()),
                    b.getPaymentStatus(), b.getStatus()
            });
        }
    }
}