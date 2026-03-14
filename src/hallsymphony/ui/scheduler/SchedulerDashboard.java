package hallsymphony.ui.scheduler;

import hallsymphony.data.*;
import hallsymphony.model.*;
import hallsymphony.util.UIHelper;
import hallsymphony.ui.MainFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * SCHEDULER DASHBOARD
 * Tab 1 – Hall Management  : Add / View+Filter / Edit / Delete halls
 * Tab 2 – Hall Schedule    : Set availability windows for halls
 * Tab 3 – Maintenance      : Set maintenance windows for halls
 */
public class SchedulerDashboard extends JFrame {

    private final Staff               me;
    private final HallFileManager     hallFM  = new HallFileManager();
    private final ScheduleFileManager schedFM = new ScheduleFileManager();

    // Hall tab widgets kept as fields so actions can reference them
    private JTextField        searchField;
    private DefaultTableModel hallModel;
    private JTable            hallTable;

    // Schedule / Maintenance tab models
    private DefaultTableModel availModel;
    private DefaultTableModel maintModel;

    public SchedulerDashboard(Staff me) {
        this.me = me;
        setTitle("Scheduler – " + me.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    /* ================================================================
       ROOT LAYOUT
    ================================================================ */
    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Top header ──────────────────────────────────────────────────
        add(UIHelper.makeHeader("Scheduler Dashboard",
                        "Logged in as: " + me.getUsername() + "   |   Role: SCHEDULER"),
                BorderLayout.NORTH);

        // ── Tabs ────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("  Hall Management  ", buildHallTab());
        tabs.addTab("  Hall Schedule (Availability)  ", buildScheduleTab("AVAILABILITY"));
        tabs.addTab("  Hall Maintenance  ", buildScheduleTab("MAINTENANCE"));
        add(tabs, BorderLayout.CENTER);

        // ── Bottom bar with logout ────────────────────────────────────
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        bar.setBackground(new Color(230, 235, 245));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 200, 230)));
        JButton logout = UIHelper.dangerBtn("  Logout  ");
        logout.addActionListener(e -> { dispose(); MainFrame.logout(); });
        bar.add(logout);
        add(bar, BorderLayout.SOUTH);
    }

    /* ================================================================
       TAB 1 – HALL MANAGEMENT
    ================================================================ */
    private JPanel buildHallTab() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIHelper.LIGHT_BG);

        /* ---- button toolbar ---- */
        searchField = UIHelper.makeField(20);
        searchField.setToolTipText("Search by hall name or type");

        JButton btnSearch  = UIHelper.secondaryBtn("Search");
        JButton btnShowAll = UIHelper.secondaryBtn("Show All");
        JButton btnAdd     = UIHelper.primaryBtn("+ Add Hall");
        JButton btnEdit    = UIHelper.warnBtn("Edit Selected");
        JButton btnDelete  = UIHelper.dangerBtn("Delete Selected");

        JPanel toolbar = UIHelper.toolbar(
                UIHelper.boldLabel("Search:"), searchField,
                btnSearch, btnShowAll,
                UIHelper.sep(),
                btnAdd, btnEdit, btnDelete
        );
        root.add(toolbar, BorderLayout.NORTH);

        /* ---- table ---- */
        String[] cols = {"Hall ID","Hall Name","Type","Capacity","Rate/hr (RM)","Description"};
        hallModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        hallTable = new JTable(hallModel);
        UIHelper.styleTable(hallTable);
        hallTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        hallTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        hallTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        hallTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        hallTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        hallTable.getColumnModel().getColumn(5).setPreferredWidth(280);
        root.add(UIHelper.scrollPane(hallTable), BorderLayout.CENTER);

        refreshHallTable("");

        /* ---- wire actions ---- */
        btnSearch.addActionListener(e  -> refreshHallTable(searchField.getText().trim()));
        searchField.addActionListener(e -> refreshHallTable(searchField.getText().trim()));
        btnShowAll.addActionListener(e -> { searchField.setText(""); refreshHallTable(""); });

        btnAdd.addActionListener(e -> openHallDialog(null));

        btnEdit.addActionListener(e -> {
            int row = hallTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this, "Please select a hall row first."); return; }
            String id = (String) hallModel.getValueAt(row, 0);
            openHallDialog(hallFM.findById(id));
        });

        btnDelete.addActionListener(e -> {
            int row = hallTable.getSelectedRow();
            if (row < 0) { UIHelper.err(this, "Please select a hall row first."); return; }
            String id   = (String) hallModel.getValueAt(row, 0);
            String name = (String) hallModel.getValueAt(row, 1);
            if (UIHelper.confirm(this, "Delete hall \"" + name + "\"? This cannot be undone.")) {
                hallFM.deleteHall(id);
                refreshHallTable("");
                UIHelper.ok(this, "Hall deleted.");
            }
        });

        return root;
    }

    private void refreshHallTable(String filter) {
        hallModel.setRowCount(0);
        String f = filter.toLowerCase();
        for (Hall h : hallFM.getAllHalls()) {
            if (f.isEmpty()
                    || h.getHallName().toLowerCase().contains(f)
                    || h.getHallType().toLowerCase().contains(f)) {
                hallModel.addRow(new Object[]{
                        h.getHallId(), h.getHallName(), h.getHallType(),
                        h.getCapacity(),
                        String.format("%.2f", h.getRatePerHour()),
                        h.getDescription()
                });
            }
        }
    }

    /** Add-or-Edit dialog for a hall */
    private void openHallDialog(Hall existing) {
        boolean edit = (existing != null);
        JDialog dlg = new JDialog(this, edit ? "Edit Hall" : "Add New Hall", true);
        dlg.setSize(500, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.LIGHT_BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JTextField     nameF = UIHelper.makeField(26);
        JComboBox<String> typeC = new JComboBox<>(new String[]{"AUDITORIUM","BANQUET_HALL","MEETING_ROOM"});
        JTextField     descF = UIHelper.makeField(26);
        JLabel         hint  = new JLabel();
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);

        Runnable updateHint = () -> {
            String t = (String) typeC.getSelectedItem();
            hint.setText("  Capacity: " + HallFileManager.getCapacityForType(t)
                    + "   Rate: RM " + String.format("%.2f", HallFileManager.getRateForType(t)) + "/hr");
        };
        typeC.addActionListener(ev -> updateHint.run());
        updateHint.run();

        if (edit) {
            nameF.setText(existing.getHallName());
            typeC.setSelectedItem(existing.getHallType());
            descF.setText(existing.getDescription());
            updateHint.run();
        }

        UIHelper.addRow(form, "Hall Name :", nameF, 0);
        UIHelper.addRow(form, "Hall Type :", typeC, 1);
        form.add(new JLabel(""), UIHelper.gbc(0, 2));
        form.add(hint,           UIHelper.gbc(1, 2));
        UIHelper.addRow(form, "Description :", descF, 3);

        // button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        JButton save   = UIHelper.primaryBtn(edit ? "Save Changes" : "Add Hall");
        JButton cancel = UIHelper.secondaryBtn("Cancel");
        btnRow.add(save); btnRow.add(cancel);

        GridBagConstraints bc = UIHelper.gbcWide(4);
        bc.insets = new Insets(18, 8, 8, 8);
        form.add(btnRow, bc);

        dlg.add(form, BorderLayout.CENTER);
        cancel.addActionListener(ev -> dlg.dispose());

        save.addActionListener(ev -> {
            String name = nameF.getText().trim();
            String type = (String) typeC.getSelectedItem();
            String desc = descF.getText().trim();
            if (name.isEmpty()) { UIHelper.err(dlg, "Hall name is required."); return; }
            if (edit) {
                existing.setHallName(name);
                existing.setHallType(type);
                existing.setCapacity(HallFileManager.getCapacityForType(type));
                existing.setRatePerHour(HallFileManager.getRateForType(type));
                existing.setDescription(desc);
                hallFM.updateHall(existing);
                UIHelper.ok(dlg, "Hall updated successfully.");
            } else {
                Hall h = new Hall(
                        hallFM.generateHallId(), name, type,
                        HallFileManager.getCapacityForType(type),
                        HallFileManager.getRateForType(type), desc
                );
                hallFM.addHall(h);
                UIHelper.ok(dlg, "Hall \"" + name + "\" added successfully.");
            }
            refreshHallTable("");
            dlg.dispose();
        });

        dlg.setVisible(true);
    }

    /* ================================================================
       TAB 2 & 3 – SCHEDULE / MAINTENANCE  (shared builder)
    ================================================================ */
    private JPanel buildScheduleTab(String type) {
        boolean isAvail = type.equals("AVAILABILITY");
        String  label   = isAvail ? "Availability" : "Maintenance";

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIHelper.LIGHT_BG);

        /* ---- entry form panel ---- */
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(UIHelper.LIGHT_BG);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIHelper.MID_BLUE, 1),
                "  Schedule Hall – " + label + "  "));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));

        JComboBox<String> hallCombo = new JComboBox<>();
        for (Hall h : hallFM.getAllHalls())
            hallCombo.addItem(h.getHallId() + " | " + h.getHallName());

        JTextField startF  = UIHelper.makeField(20);
        JTextField endF    = UIHelper.makeField(20);
        JTextArea  remarkA = new JTextArea(2, 30);
        remarkA.setFont(new Font("Arial", Font.PLAIN, 13));
        remarkA.setLineWrap(true);
        remarkA.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230)));

        startF.setText(java.time.LocalDate.now().toString() + " 08:00");
        endF.setText(java.time.LocalDate.now().plusMonths(1).toString() + " 18:00");
        startF.setToolTipText("Format: yyyy-MM-dd HH:mm   (08:00 – 18:00)");
        endF.setToolTipText("Format: yyyy-MM-dd HH:mm   (08:00 – 18:00)");

        UIHelper.addRow(form, "Hall :", hallCombo, 0);
        UIHelper.addRow(form, "Start (yyyy-MM-dd HH:mm) :", startF, 1);
        UIHelper.addRow(form, "End   (yyyy-MM-dd HH:mm) :", endF,   2);
        UIHelper.addRow(form, "Remarks :", remarkA, 3);

        // ── action buttons inside form ───────────────────────────────
        JButton btnSchedule = UIHelper.primaryBtn("Schedule Hall");
        JButton btnDelSched = UIHelper.dangerBtn("Delete Selected Schedule");

        JPanel fbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        fbtn.setOpaque(false);
        fbtn.add(btnSchedule);
        fbtn.add(btnDelSched);

        GridBagConstraints bc = UIHelper.gbcWide(4);
        bc.insets = new Insets(10, 8, 8, 8);
        form.add(fbtn, bc);

        formPanel.add(form, BorderLayout.CENTER);
        root.add(formPanel, BorderLayout.NORTH);

        /* ---- schedule list table ---- */
        String[] cols = {"Schedule ID","Hall Name","Start","End","Remarks"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        if (isAvail) availModel = model; else maintModel = model;

        JTable tbl = new JTable(model);
        UIHelper.styleTable(tbl);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(240);
        refreshScheduleTable(model, type);
        root.add(UIHelper.scrollPane(tbl), BorderLayout.CENTER);

        /* ---- wire actions ---- */
        btnSchedule.addActionListener(e -> {
            if (hallCombo.getItemCount() == 0) {
                UIHelper.err(this, "No halls found – add a hall in the Hall Management tab first.");
                return;
            }
            String hallId = ((String) hallCombo.getSelectedItem()).split(" \\| ")[0];
            String start  = startF.getText().trim();
            String end    = endF.getText().trim();
            String remark = remarkA.getText().trim();

            if (start.isEmpty() || end.isEmpty()) {
                UIHelper.err(this, "Start and End date/time are required."); return;
            }
            if (!start.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")
                    || !end.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                UIHelper.err(this, "Invalid format – use:  yyyy-MM-dd HH:mm"); return;
            }
            if (start.compareTo(end) >= 0) {
                UIHelper.err(this, "End must be after Start."); return;
            }
            int sh = Integer.parseInt(start.substring(11, 13));
            int eh = Integer.parseInt(end.substring(11, 13));
            if (sh < 8 || eh > 18) {
                UIHelper.err(this, "Operating hours are 08:00 – 18:00 only."); return;
            }
            HallSchedule hs = new HallSchedule(
                    schedFM.generateScheduleId(), hallId, type,
                    start, end, remark, me.getUserId());
            schedFM.addSchedule(hs);
            refreshScheduleTable(model, type);
            remarkA.setText("");
            UIHelper.ok(this, label + " schedule saved successfully.");
        });

        btnDelSched.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { UIHelper.err(this, "Select a schedule row to delete."); return; }
            if (UIHelper.confirm(this, "Delete this schedule entry?")) {
                schedFM.deleteSchedule((String) model.getValueAt(row, 0));
                refreshScheduleTable(model, type);
            }
        });

        return root;
    }

    private void refreshScheduleTable(DefaultTableModel model, String type) {
        model.setRowCount(0);
        for (HallSchedule s : schedFM.getAllSchedules()) {
            if (!s.getScheduleType().equals(type)) continue;
            Hall h = hallFM.findById(s.getHallId());
            model.addRow(new Object[]{
                    s.getScheduleId(),
                    h != null ? h.getHallName() : s.getHallId(),
                    s.getStartDateTime(), s.getEndDateTime(), s.getRemarks()
            });
        }
    }
}