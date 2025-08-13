package bokchi.java.config;

import bokchi.java.ui.staff.StaffLoginFrame;
import bokchi.java.ui.user.UserLoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
//            new StaffLoginFrame().setVisible(true);
          new UserLoginFrame().setVisible(true);
        });
    }
}