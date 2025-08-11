package bokchi.java.ui.staff;

import bokchi.java.dao.jdbc.JdbcUserDaoImple;
import bokchi.java.model.UserVO;
import bokchi.java.model.enums.Role;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StaffLoginFrame extends JFrame {
    private final JTextField tfUsername = new JTextField(15);
    private final JPasswordField pfPassword = new JPasswordField(15);
    private final JButton btnLogin = new JButton("로그인");

    public StaffLoginFrame() {
        super("STAFF 로그인");
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
        if (user.getRole() != Role.STAFF) {
            JOptionPane.showMessageDialog(this, "직원 계정이 아닙니다.");
            return;
        }

        // 로그인 성공 → 메인 오픈
        SwingUtilities.invokeLater(() -> new StaffMainFrame(user).setVisible(true));
        dispose();
    }
}