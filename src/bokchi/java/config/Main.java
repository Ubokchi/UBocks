package bokchi.java.config;

import bokchi.java.ui.staff.StaffLoginFrame;
import bokchi.java.ui.user.UserLoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 둘 중 하나를 켜서 테스트
//            new StaffLoginFrame().setVisible(true);
          new UserLoginFrame().setVisible(true);
        });
    }
}