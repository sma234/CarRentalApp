package gui.dialogs;

import api.model.Employee;

import javax.swing.*;
import java.awt.*;

public class UserDialog extends JDialog {
    private boolean ok = false;

    private final JTextField fullNameField = new JTextField(18);
    private final JTextField usernameField = new JTextField(14);
    private final JTextField emailField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(14);

    public UserDialog(Window owner) {
        super(owner, "Νέος Χρήστης", ModalityType.APPLICATION_MODAL);
        setSize(450, 300);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addRow(form, gc, y++, "Ονοματεπώνυμο:", fullNameField);
        addRow(form, gc, y++, "Username:", usernameField);
        addRow(form, gc, y++, "Email:", emailField);
        addRow(form, gc, y++, "Password:", passwordField);

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

    public Employee getEmployee() {
        return new Employee(
                fullNameField.getText().trim(),
                usernameField.getText().trim(),
                emailField.getText().trim(),
                new String(passwordField.getPassword())
        );
    }
}
