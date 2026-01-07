package gui.dialogs;

import api.model.Customer;

import javax.swing.*;
import java.awt.*;

public class CustomerDialog extends JDialog {
    private boolean ok = false;

    private final JTextField afmField = new JTextField(10);
    private final JTextField nameField = new JTextField(18);
    private final JTextField phoneField = new JTextField(12);
    private final JTextField emailField = new JTextField(18);

    public CustomerDialog(Window owner, Customer existing) {
        super(owner, "Πελάτης", ModalityType.APPLICATION_MODAL);
        setSize(450, 300);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addRow(form, gc, y++, "ΑΦΜ:", afmField);
        addRow(form, gc, y++, "Ονοματεπώνυμο:", nameField);
        addRow(form, gc, y++, "Τηλέφωνο:", phoneField);
        addRow(form, gc, y++, "Email:", emailField);

        if (existing != null) {
            afmField.setText(existing.getAfm());
            afmField.setEnabled(false); // key
            nameField.setText(existing.getFullName());
            phoneField.setText(existing.getPhone());
            emailField.setText(existing.getEmail());
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

    public Customer getCustomer() {
        return new Customer(
                afmField.getText().trim(),
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim()
        );
    }
}
