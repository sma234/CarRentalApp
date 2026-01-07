package gui;

import api.model.Employee;
import api.service.CarRentalSystem;
import gui.panels.CarPanel;
import gui.panels.CustomerPanel;
import gui.panels.RentalPanel;
import gui.panels.UserPanel;
import gui.util.Ui;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame{
    private final CarRentalSystem system;
    private final Employee employee;

    public MainFrame(CarRentalSystem system,Employee employee){
        super("Car Rental - " + employee.getFullName());
        this.system = system;
        this.employee = employee;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Αυτοκίνητα", new CarPanel(system));
        tabs.addTab("Πελάτες", new CustomerPanel(system));
        tabs.addTab("Ενοικιάσεις", new RentalPanel(system));
        tabs.addTab("Χρήστες", new UserPanel(system));

        setLayout(new BorderLayout());
        add(buildTopBar(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JComponent buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JLabel who = new JLabel("Συνδεδεμένος: " + employee.getFullName() + " (" + employee.getUsername() + ")");
        p.add(who, BorderLayout.WEST);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            if (!Ui.confirm(this, "Αποσύνδεση;")) return;
            system.logout();
            new LoginFrame(system).setVisible(true);
            dispose();
        });
        p.add(logout, BorderLayout.EAST);
        return p;
    }
}
