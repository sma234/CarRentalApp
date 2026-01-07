package gui.panels;

import api.model.Employee;
import api.service.CarRentalSystem;
import api.service.ValidationException;
import gui.dialogs.UserDialog;
import gui.util.Ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserPanel extends JPanel {
    private final CarRentalSystem system;

    private final UserTableModel tableModel = new UserTableModel();
    private final JTable table = new JTable(tableModel);

    public UserPanel(CarRentalSystem system) {
        super(new BorderLayout(8,8));
        this.system = system;

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        refreshAll();
    }

    private JComponent buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Προσθήκη Χρήστη");
        JButton del = new JButton("Διαγραφή Χρήστη");

        add.addActionListener(e -> onAdd());
        del.addActionListener(e -> onDelete());

        p.add(add);
        p.add(del);
        return p;
    }

    private void refreshAll() {
        tableModel.setRows(system.getEmployees());
    }

    private Employee selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getAt(row);
    }

    private void onAdd() {
        UserDialog d = new UserDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (!d.isOk()) return;

        try {
            system.addEmployee(d.getEmployee());
            Ui.info(this, "Ο χρήστης προστέθηκε.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private void onDelete() {
        Employee e = selected();
        if (e == null) { Ui.error(this, "Επιλέξτε χρήστη."); return; }

        if (!Ui.confirm(this, "Διαγραφή χρήστη " + e.getUsername() + ";")) return;

        try {
            system.deleteEmployee(e.getUsername());
            Ui.info(this, "Ο χρήστης διαγράφηκε.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private static class UserTableModel extends AbstractTableModel {
        private final String[] cols = {"Ονοματεπώνυμο", "Username", "Email"};
        private List<Employee> rows = new ArrayList<>();

        public void setRows(List<Employee> rows) {
            this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
            fireTableDataChanged();
        }

        public Employee getAt(int r) { return rows.get(r); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Employee e = rows.get(rowIndex);
            switch (columnIndex) {
                case 0: return e.getFullName();
                case 1: return e.getUsername();
                case 2: return e.getEmail();
                default: return "";
            }
        }
    }
}
