package bokchi.java.ui.user;

import bokchi.java.dao.jdbc.JdbcUserDaoImple;
import bokchi.java.model.UserVO;
import bokchi.java.model.enums.Role;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLIntegrityConstraintViolationException;

public class UserSignUpFrame extends JFrame {
    private final JTextField tfUsername = new JTextField(15);
    private final JPasswordField pfPassword = new JPasswordField(15);
    private final JPasswordField pfPassword2 = new JPasswordField(15);
    private final JTextField tfName = new JTextField(15);
    private final JTextField tfPhone = new JTextField(15);
    private final JButton btnSignUp = new JButton("가입");
    private final JButton btnCancel = new JButton("취소");

    public UserSignUpFrame() {
        super("회원가입");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(380, 280);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("아이디"));
        form.add(tfUsername);
        form.add(new JLabel("비밀번호"));
        form.add(pfPassword);
        form.add(new JLabel("비밀번호 확인"));
        form.add(pfPassword2);
        form.add(new JLabel("이름"));
        form.add(tfName);
        form.add(new JLabel("전화번호"));
        form.add(tfPhone);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancel);
        buttons.add(btnSignUp);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        btnSignUp.addActionListener(this::onSignUp);
        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSignUp);
    }

    private void onSignUp(ActionEvent e) {
        String username = tfUsername.getText().trim();
        String pw1 = new String(pfPassword.getPassword());
        String pw2 = new String(pfPassword2.getPassword());
        String name = tfName.getText().trim();
        String phone = tfPhone.getText().trim();

        if (username.isEmpty() || pw1.isEmpty() || pw2.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 항목을 입력하세요.");
            return;
        }
        if (!pw1.equals(pw2)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
            pfPassword.setText(""); pfPassword2.setText("");
            pfPassword.requestFocus();
            return;
        }
        if (!phone.matches("\\d{2,3}-\\d{3,4}-\\d{4}") && !phone.matches("\\d{10,11}")) {
            int ans = JOptionPane.showConfirmDialog(this, "전화번호 형식이 일반적이지 않습니다. 계속할까요?", "확인", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;
        }

        // 2) UserVO 생성
        UserVO vo = new UserVO();
        vo.setUsername(username);
        vo.setPassword(pw1);                
        vo.setRole(Role.CUSTOMER);       
        vo.setName(name);
        vo.setPhone(phone);
        vo.setRewardBalance(0);

        var dao = JdbcUserDaoImple.getInstance();

        var exists = dao.findByUsername(username);
        if (exists != null) {
            JOptionPane.showMessageDialog(this, "이미 존재하는 아이디입니다.");
            tfUsername.requestFocus();
            return;
        }

        try {
            int inserted = dao.insert(vo);
            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "가입이 완료되었습니다. 로그인해주세요.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "가입에 실패했습니다. 입력값을 확인해주세요.");
            }
        } catch (Exception ex) {
            if (ex.getCause() instanceof SQLIntegrityConstraintViolationException
                || ex instanceof SQLIntegrityConstraintViolationException) {
                JOptionPane.showMessageDialog(this, "중복된 전화번호입니다. 다른 번호를 사용하세요.");
            } else {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "오류가 발생했습니다: " + ex.getMessage());
            }
        }
    }
}