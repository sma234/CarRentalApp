package gui.panels;

import api.model.Car;
import api.model.CarStatus;
import api.service.CarRentalSystem;
import api.service.ValidationException;
import gui.dialogs.CarDialog;
import gui.util.Ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CarPanel extends JPanel {
    private final CarRentalSystem system;

    private final JTextField brandField = new JTextField(10);
    private final JTextField plateField = new JTextField(8);
    private final JTextField modelField = new JTextField(10);
    private final JTextField colorField = new JTextField(10);
    private final JTextField typeField = new JTextField(10);
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"(Όλα)", "Διαθέσιμο", "Ενοικιασμένο"});

    private final CarTableModel tableModel = new CarTableModel();
    private final JTable table = new JTable(tableModel);

    public CarPanel(CarRentalSystem system) {
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

        p.add(new JLabel("Μάρκα:"));
        p.add(brandField);
        p.add(new JLabel("Πινακίδα:"));
        p.add(plateField);
        p.add(new JLabel("Μοντέλο:"));
        p.add(modelField);
        p.add(new JLabel("Χρώμα:"));
        p.add(colorField);
        p.add(new JLabel("Τύπος:"));
        p.add(typeField);
        p.add(new JLabel("Κατάσταση:"));
        p.add(statusBox);

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.addActionListener(e -> doSearch());
        JButton clearBtn = new JButton("Καθαρισμός");
        clearBtn.addActionListener(e -> {
            brandField.setText(""); plateField.setText(""); modelField.setText(""); colorField.setText(""); typeField.setText("");
            statusBox.setSelectedIndex(0);
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
        tableModel.setRows(system.getCars());
    }

    private void doSearch() {
        CarStatus status = null;
        if (statusBox.getSelectedIndex() == 1) status = CarStatus.AVAILABLE;
        if (statusBox.getSelectedIndex() == 2) status = CarStatus.RENTED;

        List<Car> res = system.searchCars(
                brandField.getText(),
                plateField.getText(),
                modelField.getText(),
                colorField.getText(),
                typeField.getText(),
                status
        );
        tableModel.setRows(res);
    }

    private Car selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getAt(row);
    }

    private void onAdd() {
        CarDialog d = new CarDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (!d.isOk()) return;

        try {
            system.addCar(d.getCar());
            Ui.info(this, "Το αυτοκίνητο προστέθηκε.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private void onEdit() {
        Car c = selected();
        if (c == null) { Ui.error(this, "Επιλέξτε αυτοκίνητο."); return; }

        CarDialog d = new CarDialog(SwingUtilities.getWindowAncestor(this), c);
        d.setVisible(true);
        if (!d.isOk()) return;

        try {
            system.updateCar(d.getCar());
            Ui.info(this, "Οι αλλαγές αποθηκεύτηκαν.");
            refreshAll();
        } catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        } catch (IOException ex) {
            Ui.error(this, "Σφάλμα αποθήκευσης: " + ex.getMessage());
        }
    }

    private static class CarTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Πινακίδα", "Μάρκα", "Τύπος", "Μοντέλο", "Έτος", "Χρώμα", "Κατάσταση"};
        private List<Car> rows = new ArrayList<>();

        public void setRows(List<Car> rows) {
            this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
            fireTableDataChanged();
        }

        public Car getAt(int r) { return rows.get(r); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Car c = rows.get(rowIndex);
            switch (columnIndex) {
                case 0: return c.getId();
                case 1: return c.getPlate();
                case 2: return c.getBrand();
                case 3: return c.getType();
                case 4: return c.getModel();
                case 5: return c.getYear();
                case 6: return c.getColor();
                case 7: return c.getStatus().toGreek();
                default: return "";
            }
        }
    }
}
