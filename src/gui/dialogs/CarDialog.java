package gui.dialogs;

import api.model.Car;
import api.model.CarStatus;

import javax.swing.*;
import java.awt.*;

public class CarDialog extends JDialog {
    private boolean ok = false;

    private final JTextField idField = new JTextField(8);
    private final JTextField plateField = new JTextField(10);
    private final JTextField brandField = new JTextField(12);
    private final JTextField typeField = new JTextField(12);
    private final JTextField modelField = new JTextField(12);
    private final JTextField yearField = new JTextField(6);
    private final JTextField colorField = new JTextField(10);
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"Διαθέσιμο", "Ενοικιασμένο"});

    public CarDialog(Window owner, Car existing) {
        super(owner, "Αυτοκίνητο", ModalityType.APPLICATION_MODAL);
        setSize(420, 360);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addRow(form, gc, y++, "ID:", idField);
        addRow(form, gc, y++, "Πινακίδα:", plateField);
        addRow(form, gc, y++, "Μάρκα:", brandField);
        addRow(form, gc, y++, "Τύπος:", typeField);
        addRow(form, gc, y++, "Μοντέλο:", modelField);
        addRow(form, gc, y++, "Έτος:", yearField);
        addRow(form, gc, y++, "Χρώμα:", colorField);
        addRow(form, gc, y++, "Κατάσταση:", statusBox);

        if (existing != null) {
            idField.setText(String.valueOf(existing.getId()));
            idField.setEnabled(false); // id as key
            plateField.setText(existing.getPlate());
            brandField.setText(existing.getBrand());
            typeField.setText(existing.getType());
            modelField.setText(existing.getModel());
            yearField.setText(String.valueOf(existing.getYear()));
            colorField.setText(existing.getColor());
            statusBox.setSelectedIndex(existing.getStatus() == CarStatus.AVAILABLE ? 0 : 1);
        }

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        okBtn.addActionListener(e -> { ok = true; dispose(); });
        cancelBtn.addActionListener(e -> dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(cancelBtn);
        south.add(okBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okBtn);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int y, String label, Component field) {
        gc.gridx = 0; gc.gridy = y;
        p.add(new JLabel(label), gc);
        gc.gridx = 1;
        p.add(field, gc);
    }

    public boolean isOk() { return ok; }

    public Car getCar() {
        int id = parseInt(idField.getText());
        int year = parseInt(yearField.getText());
        CarStatus status = statusBox.getSelectedIndex() == 0 ? CarStatus.AVAILABLE : CarStatus.RENTED;
        return new Car(id, plateField.getText().trim(), brandField.getText().trim(), typeField.getText().trim(),
                modelField.getText().trim(), year, colorField.getText().trim(), status);
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return -1; }
    }
}
