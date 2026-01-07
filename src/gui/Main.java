package gui;

import api.service.CarRentalSystem;
import api.storage.DataStore;
import javax.swing.*;
import java.io.IOException;

public class Main{
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            try{
                CarRentalSystem system = new CarRentalSystem(new DataStore());
                new LoginFrame(system).setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Σφάλμα φόρτωσης δεδομένων: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
