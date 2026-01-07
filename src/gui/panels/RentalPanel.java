package gui.panels;

import api.model.Car;
import api.model.Customer;
import api.model.Rental;
import api.service.CarRentalSystem;
import api.service.ValidationException;
import gui.dialogs.RentalDialog;
import gui.util.Ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RentalPanel extends JPanel {
    private final CarRentalSystem system;

    private final RentalTableModel tableModel = new RentalTableModel();
    private final JTable table = new JTable(tableModel);

    private final JTextField customerAfmField = new JTextField(10);
    private final JTextField carPlateField = new JTextField(8);

    public RentalPanel(CarRentalSystem system) {
        super(new BorderLayout(8,8));
        this.system = system;

        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        refreshAll();
    }

    private JComponent buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        JLabel hint = new JLabel("Εμφανίζονται όλες οι ενοικιάσεις (ενεργές + ιστορικό).");
        p.add(hint, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filters.add(new JLabel("Φίλτρο ΑΦΜ:"));
        filters.add(customerAfmField);
        filters.add(new JLabel("Φίλτρο Πινακίδας:"));
        filters.add(carPlateField);

        JButton apply = new JButton("Εφαρμογή");
        apply.addActionListener(e -> applyFilter());
        JButton clear = new JButton("Καθαρισμός");
        clear.addActionListener(e -> { customerAfmField.setText(""); carPlateField.setText(""); refreshAll(); });

        filters.add(apply);
        filters.add(clear);

        p.add(filters, BorderLayout.EAST);
        return p;
    }

    private JComponent buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newRental = new JButton("Νέα Ενοικίαση");
        JButton returnBtn = new JButton("Επιστροφή");
        JButton historyCustomer = new JButton("Ιστορικό Πελάτη");
        JButton historyCar = new JButton("Ιστορικό Αυτοκινήτου");

        newRental.addActionListener(e -> onNewRental());
        returnBtn.addActionListener(e -> onReturn());
        historyCustomer.addActionListener(e -> onHistoryCustomer());
        historyCar.addActionListener(e -> onHistoryCar());

        p.add(newRental);
        p.add(returnBtn);
        p.add(historyCustomer);
        p.add(historyCar);
        return p;
    }

    private void refreshAll() {
        tableModel.setRows(system.getRentals(), system);
    }

    private void applyFilter() {
        String afm = customerAfmField.getText().trim();
        String plate = carPlateField.getText().trim();

        List<Rental> all = system.getRentals();
        List<Rental> filtered = new ArrayList<>();
        for (Rental r : all) {
            boolean ok = true;
            if (!afm.isEmpty() && !r.getCustomerAfm().contains(afm)) ok = false;
            if (!plate.isEmpty()) {
                Car c = system.findCarById(r.getCarId());
                if (c == null || !c.getPlate().toLowerCase().contains(plate.toLowerCase())) ok = false;
            }
            if (ok) filtered.add(r);
        }
        tableModel.setRows(filtered, system);
    }

    private Rental selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getAt(row);
    }

    private void onNewRental() {
        RentalDialog d = new RentalDialog(SwingUtilities.getWindowAncestor(this), system);
        d.setVisible(true);
        if (!d.isOk()) return;

        try {
            system.rentCar(d.getCarId(), d.getCustomerAfm(), d.getStartDate(), d.getEndDate());
            Ui.info(this, "Η ενοικίαση καταχωρήθηκε.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private void onReturn() {
        Rental r = selected();
        if (r == null) { Ui.error(this, "Επιλέξτε ενοικίαση."); return; }
        if (r.isReturned()) { Ui.error(this, "Η ενοικίαση είναι ήδη ολοκληρωμένη."); return; }

        if (!Ui.confirm(this, "Ολοκλήρωση επιστροφής για rentalId=" + r.getRentalId() + ";")) return;

        try {
            system.returnRental(r.getRentalId());
            Ui.info(this, "Η επιστροφή ολοκληρώθηκε.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private void onHistoryCustomer() {
        String afm = JOptionPane.showInputDialog(this, "Δώσε ΑΦΜ πελάτη:");
        if (afm == null || afm.trim().isEmpty()) return;
        Customer c = system.findCustomerByAfm(afm.trim());
        if (c == null) { Ui.error(this, "Ο πελάτης δεν βρέθηκε."); return; }

        List<Rental> rs = system.getRentalsForCustomer(c.getAfm());
        tableModel.setRows(rs, system);
    }

    private void onHistoryCar() {
        String plate = JOptionPane.showInputDialog(this, "Δώσε πινακίδα αυτοκινήτου:");
        if (plate == null || plate.trim().isEmpty()) return;
        Car car = system.findCarByPlate(plate.trim());
        if (car == null) { Ui.error(this, "Το αυτοκίνητο δεν βρέθηκε."); return; }

        List<Rental> rs = system.getRentalsForCar(car.getId());
        tableModel.setRows(rs, system);
    }

    private static class RentalTableModel extends AbstractTableModel {
        private final String[] cols = {"Rental ID", "Πινακίδα", "Πελάτης(ΑΦΜ)", "Υπάλληλος", "Έναρξη", "Λήξη", "Κατάσταση", "Επιστροφή"};
        private List<Rental> rows = new ArrayList<>();
        private List<String[]> cache = new ArrayList<>();
        private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        public void setRows(List<Rental> rows, CarRentalSystem system) {
            this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
            cache = new ArrayList<>();
            for (Rental r : this.rows) {
                Car car = system.findCarById(r.getCarId());
                String plate = car == null ? ("#" + r.getCarId()) : car.getPlate();
                String status = r.isReturned() ? "Ολοκληρωμένη" : "Ενεργή";
                String retDate = r.getActualReturnDate() == null ? "" : r.getActualReturnDate().format(fmt);
                cache.add(new String[]{
                        String.valueOf(r.getRentalId()),
                        plate,
                        r.getCustomerAfm(),
                        r.getEmployeeUsername(),
                        r.getStartDate().format(fmt),
                        r.getEndDate().format(fmt),
                        status,
                        retDate
                });
            }
            fireTableDataChanged();
        }

        public Rental getAt(int r) { return rows.get(r); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return cache.get(rowIndex)[columnIndex];
        }
    }
}
