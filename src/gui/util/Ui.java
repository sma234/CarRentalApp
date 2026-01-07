package gui.util;

import javax.swing.*;
import java.awt.*;

public final class Ui {
    private Ui(){}

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Σφάλμα", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Πληροφορία", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Επιβεβαίωση", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
