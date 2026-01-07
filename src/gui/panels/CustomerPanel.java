package gui.panels;

import api.model.Customer;
import api.service.CarRentalSystem;
import api.service.ValidationException;
import gui.dialogs.CustomerDialog;
import gui.util.Ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel {
    private final CarRentalSystem system;

    private final JTextField afmField = new JTextField(10);
    private final JTextField nameField = new JTextField(14);
    private final JTextField phoneField = new JTextField(10);

    private final CustomerTableModel tableModel = new CustomerTableModel();
    private final JTable table = new JTable(tableModel);

    public CustomerPanel(CarRentalSystem system) {
        super(new BorderLayout(8,8));
        this.system = system;

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        refreshAll();
    }

    private JComponent buildSearchBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder("Αναζήτηση"));

        p.add(new JLabel("ΑΦΜ:"));
        p.add(afmField);
        p.add(new JLabel("Όνομα:"));
        p.add(nameField);
        p.add(new JLabel("Τηλέφωνο:"));
        p.add(phoneField);

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> doSearch());
        JButton clearBtn = new JButton("Καθαρισμός");
        clearBtn.addActionListener(e -> {
            afmField.setText(""); nameField.setText(""); phoneField.setText("");
            refreshAll();
        });

        p.add(searchBtn);
        p.add(clearBtn);
        return p;
    }

    private JComponent buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Προσθήκη");
        JButton edit = new JButton("Επεξεργασία");

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());

        p.add(add);
        p.add(edit);
        return p;
    }

    private void refreshAll() {
        tableModel.setRows(system.getCustomers());
    }

    private void doSearch() {
        tableModel.setRows(system.searchCustomers(afmField.getText(), nameField.getText(), phoneField.getText()));
    }

    private Customer selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getAt(row);
    }

    private void onAdd() {
        CustomerDialog d = new CustomerDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (!d.isOk()) return;

        try {
            system.addCustomer(d.getCustomer());
            Ui.info(this, "Ο πελάτης προστέθηκε.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private void onEdit() {
        Customer c = selected();
        if (c == null) { Ui.error(this, "Επιλέξτε πελάτη."); return; }

        CustomerDialog d = new CustomerDialog(SwingUtilities.getWindowAncestor(this), c);
        d.setVisible(true);
        if (!d.isOk()) return;

        try {
            system.updateCustomer(d.getCustomer());
            Ui.info(this, "Οι αλλαγές αποθηκεύτηκαν.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private static class CustomerTableModel extends AbstractTableModel {
        private final String[] cols = {"ΑΦΜ", "Ονοματεπώνυμο", "Τηλέφωνο", "Email"};
        private List<Customer> rows = new ArrayList<>();

        public void setRows(List<Customer> rows) {
            this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
            fireTableDataChanged();
        }

        public Customer getAt(int r) { return rows.get(r); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Customer c = rows.get(rowIndex);
            switch (columnIndex) {
                case 0: return c.getAfm();
                case 1: return c.getFullName();
                case 2: return c.getPhone();
                case 3: return c.getEmail();
                default: return "";
            }
        }
    }
}
