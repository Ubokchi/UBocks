package bokchi.java.ui.user;

import bokchi.java.dao.jdbc.JdbcUserDaoImple;
import bokchi.java.model.UserVO;
import bokchi.java.model.enums.Role;
import bokchi.java.ui.staff.StaffLoginFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserLoginFrame extends JFrame {
    private final JTextField tfUsername = new JTextField(15);
    private final JPasswordField pfPassword = new JPasswordField(15);
    private final JButton btnLogin = new JButton("로그인");

    private static final int HITBOX = 48;           
    private static final int LONG_PRESS_MS = 5000;  // 5초

    public UserLoginFrame() {
        super("CUSTOMER 로그인");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 200);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("아이디:"));
        form.add(tfUsername);
        form.add(new JLabel("비밀번호:"));
        form.add(pfPassword);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        root.add(form, BorderLayout.CENTER);
        root.add(btnLogin, BorderLayout.SOUTH);
        setContentPane(root);

        // 비밀 전환 오버레이 
        installSecretSwitchOverlay();

        btnLogin.addActionListener(this::onLogin);
        getRootPane().setDefaultButton(btnLogin);
    }

    private void onLogin(ActionEvent e) {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디/비밀번호를 입력하세요.");
            return;
        }

        var dao = JdbcUserDaoImple.getInstance();
        UserVO user = dao.findByUsername(username);
        if (user == null || user.getPassword() == null) {
            JOptionPane.showMessageDialog(this, "존재하지 않는 계정입니다.");
            return;
        }
        if (!password.equals(user.getPassword())) {
            JOptionPane.showMessageDialog(this, "비밀번호가 올바르지 않습니다.");
            return;
        }
        if (user.getRole() != Role.CUSTOMER) {
            JOptionPane.showMessageDialog(this, "고객 계정이 아닙니다.");
            return;
        }

        SwingUtilities.invokeLater(() -> new UserMainFrame(user).setVisible(true));
        dispose();
    }

    private void installSecretSwitchOverlay() {
        JRootPane rp = getRootPane();
        JComponent glass = (JComponent) rp.getGlassPane();
        glass.setVisible(true);
        glass.setOpaque(false);

        final Timer[] holdTimer = new Timer[1];

        glass.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (inTopRightCorner(e)) {
                    // 롱프레스 타이머 시작
                    holdTimer[0] = new Timer(LONG_PRESS_MS, ev -> switchToStaffLogin());
                    holdTimer[0].setRepeats(false);
                    holdTimer[0].start();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 손 떼면 롱프레스 취소
                if (holdTimer[0] != null && holdTimer[0].isRunning()) {
                    holdTimer[0].stop();
                }
            }
        });
    }

    private boolean inTopRightCorner(MouseEvent e) {
        // glassPane 좌표 → contentPane 좌표로 변환
        Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), getContentPane());
        int w = getContentPane().getWidth();
        int xFromRight = w - p.x;
        int y = p.y;
        return (xFromRight >= 0 && xFromRight <= HITBOX) && (y >= 0 && y <= HITBOX);
    }

    /** 스태프 로그인 화면으로 전환 */
    private void switchToStaffLogin() {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new StaffLoginFrame().setVisible(true);
        });
    }
}