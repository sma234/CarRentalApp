package gui.dialogs;

import api.model.Car;
import api.model.CarStatus;
import api.model.Customer;
import api.service.CarRentalSystem;
import gui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDialog extends JDialog {
    private boolean ok = false;

    private final JComboBox<Car> carBox = new JComboBox<>();
    private final JComboBox<Customer> customerBox = new JComboBox<>();
    private final JTextField startField = new JTextField(10); // yyyy-MM-dd
    private final JTextField endField = new JTextField(10);

    private final CarRentalSystem system;

    public RentalDialog(Window owner, CarRentalSystem system) {
        super(owner, "Νέα Ενοικίαση", ModalityType.APPLICATION_MODAL);
        this.system = system;
        setSize(520, 280);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        loadCombos();

        startField.setText(LocalDate.now().toString());
        endField.setText(LocalDate.now().plusDays(3).toString());

        int y = 0;
        addRow(form, gc, y++, "Αυτοκίνητο (διαθέσιμα):", carBox);
        addRow(form, gc, y++, "Πελάτης:", customerBox);
        addRow(form, gc, y++, "Ημ/νία έναρξης (yyyy-MM-dd):", startField);
        addRow(form, gc, y++, "Ημ/νία λήξης (yyyy-MM-dd):", endField);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        okBtn.addActionListener(e -> onOk());
        cancelBtn.addActionListener(e -> dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(cancelBtn);
        south.add(okBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okBtn);
    }

    private void loadCombos() {
        List<Car> available = new ArrayList<>();
        for (Car c : system.getCars()) {
            if (c.getStatus() == CarStatus.AVAILABLE) available.add(c);
        }
        DefaultComboBoxModel<Car> carModel = new DefaultComboBoxModel<>(available.toArray(new Car[0]));
        carBox.setModel(carModel);

        List<Customer> customers = system.getCustomers();
        DefaultComboBoxModel<Customer> custModel = new DefaultComboBoxModel<>(customers.toArray(new Customer[0]));
        customerBox.setModel(custModel);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int y, String label, Component field) {
        gc.gridx = 0; gc.gridy = y;
        p.add(new JLabel(label), gc);
        gc.gridx = 1;
        p.add(field, gc);
    }

    private void onOk() {
        if (carBox.getSelectedItem() == null) {
            Ui.error(this, "Δεν υπάρχουν διαθέσιμα αυτοκίνητα.");
            return;
        }
        if (customerBox.getSelectedItem() == null) {
            Ui.error(this, "Δεν υπάρχουν πελάτες.");
            return;
        }
        // basic date validation (real validation in service)
        try {
            LocalDate.parse(startField.getText().trim());
            LocalDate.parse(endField.getText().trim());
        } catch (Exception ex) {
            Ui.error(this, "Δώστε σωστές ημερομηνίες (yyyy-MM-dd).");
            return;
        }

        ok = true;
        dispose();
    }

    public boolean isOk() { return ok; }

    public int getCarId() {
        Car c = (Car) carBox.getSelectedItem();
        return c.getId();
    }

    public String getCustomerAfm() {
        Customer c = (Customer) customerBox.getSelectedItem();
        return c.getAfm();
    }

    public LocalDate getStartDate() { return LocalDate.parse(startField.getText().trim()); }
    public LocalDate getEndDate() { return LocalDate.parse(endField.getText().trim()); }
}
