package gui;

import api.model.Employee;
import api.service.CarRentalSystem;
import api.service.ValidationException;
import gui.util.Ui;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame{
    private final CarRentalSystem system;

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginFrame(CarRentalSystem system){
        super("Car Rental - Login");
        this.system = system;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 220);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Σύνδεση Υπαλλήλου");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        form.add(usernameField, gc);

        gc.gridx = 0; gc.gridy = 1;
        form.add(new JLabel("Password:"), gc);
        gc.gridx = 1;
        form.add(passwordField, gc);

        root.add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginBtn);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(loginBtn);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void doLogin(){
        try {
            Employee emp = system.login(usernameField.getText(), new String(passwordField.getPassword()));
            MainFrame mf = new MainFrame(system, emp);
            mf.setVisible(true);
            dispose();
        }catch (ValidationException ex) {
            Ui.error(this, ex.getMessage());
        }catch (Exception ex) {
            Ui.error(this, "Απρόσμενο σφάλμα: " + ex.getMessage());
        }
    }
}
