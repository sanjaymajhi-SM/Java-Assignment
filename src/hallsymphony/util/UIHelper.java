package hallsymphony.util;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class UIHelper {

    public static final Color DARK_BLUE = new Color(31,  56, 100);
    public static final Color MID_BLUE  = new Color(46, 117, 182);
    public static final Color LIGHT_BG  = new Color(245, 248, 255);
    public static final Color RED       = new Color(192,  40,  40);
    public static final Color GREEN     = new Color(34,  139,  34);
    public static final Color ORANGE    = new Color(200, 100,   0);

    // ── Buttons ───────────────────────────────────────────────────────────
    private static JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    public static JButton primaryBtn(String t)   { return btn(t, DARK_BLUE); }
    public static JButton secondaryBtn(String t) { return btn(t, MID_BLUE);  }
    public static JButton dangerBtn(String t)    { return btn(t, RED);       }
    public static JButton successBtn(String t)   { return btn(t, GREEN);     }
    public static JButton warnBtn(String t)      { return btn(t, ORANGE);    }

    // ── Fields ────────────────────────────────────────────────────────────
    public static JTextField makeField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 230)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }
    public static JPasswordField makePasswordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 230)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    // ── Labels ────────────────────────────────────────────────────────────
    public static JLabel titleLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 22));
        l.setForeground(DARK_BLUE);
        return l;
    }
    public static JLabel boldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        return l;
    }

    // ── Header panel ──────────────────────────────────────────────────────
    public static JPanel makeHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(DARK_BLUE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 20));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Arial", Font.PLAIN, 12));
        s.setForeground(new Color(180, 210, 255));
        JPanel txt = new JPanel(new GridLayout(2, 1));
        txt.setOpaque(false);
        txt.add(t); txt.add(s);
        p.add(txt, BorderLayout.WEST);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.setGridColor(new Color(210, 225, 245));
        table.setSelectionBackground(new Color(46, 117, 182, 100));
        table.setSelectionForeground(Color.BLACK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? new Color(235, 244, 255) : Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Arial", Font.BOLD, 13));
        h.setBackground(DARK_BLUE);
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0, 32));
        h.setReorderingAllowed(false);
    }

    public static JScrollPane scrollPane(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230)));
        return sp;
    }

    // ── Button toolbar (horizontal strip) ────────────────────────────────
    public static JPanel toolbar(JComponent... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(new Color(235, 241, 252));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 200, 230)));
        for (JComponent c : items) p.add(c);
        return p;
    }

    // ── Separator ─────────────────────────────────────────────────────────
    public static JSeparator sep() { return new JSeparator(); }

    // ── Dialogs ───────────────────────────────────────────────────────────
    public static void ok(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void err(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // keep old names for compatibility
    public static void showSuccess(Component p, String m) { ok(p, m); }
    public static void showError(Component p, String m)   { err(p, m); }

    // ── GridBag helpers ───────────────────────────────────────────────────
    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y;
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        return g;
    }
    public static GridBagConstraints gbcWide(int y) {
        GridBagConstraints g = gbc(0, y);
        g.gridwidth = 2;
        g.weightx   = 1.0;
        return g;
    }

    // ── Simple form row ───────────────────────────────────────────────────
    /** Add a label+field pair to a GridBagLayout panel at row y */
    public static void addRow(JPanel p, String label, JComponent field, int y) {
        p.add(boldLabel(label), gbc(0, y));
        p.add(field,            gbc(1, y));
    }
}
