package bokchi.java.ui.user;

import bokchi.java.dao.jdbc.JdbcUserDaoImple;
import bokchi.java.model.UserVO;
import bokchi.java.model.enums.Role;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLIntegrityConstraintViolationException;

public class UserSignUpFrame extends JFrame {
    private final JTextField tfUsername = new JTextField(18);
    private final JPasswordField pfPassword = new JPasswordField(18);
    private final JPasswordField pfPassword2 = new JPasswordField(18);
    private final JTextField tfName = new JTextField(18);
    private final JTextField tfPhone = new JTextField(18);
    private final JCheckBox cbShowPw = new JCheckBox("비밀번호 표시");
    private final JButton btnSignUp = new JButton("가입");
    private final JButton btnCancel = new JButton("취소");
    // 중복체크 버튼
    private final JButton btnCheckId = new JButton("중복체크");
    private final JButton btnCheckPhone = new JButton("중복체크");

    // 브랜드 컬러 (로그인과 동일 톤)
    private static final Color BRAND = new Color(0x006241);
    private static final Color BRAND_DARK = new Color(0x004D33);
    private static final Color BG = new Color(0xF6F7F6);

    public UserSignUpFrame() {
        super("Star Bokchi · 회원가입");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 600);
        setLocationRelativeTo(null);

        setContentPane(buildRoot());
        wireEvents();
        getRootPane().setDefaultButton(btnSignUp);
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCard(), BorderLayout.CENTER);
        return root;
    }
    
    //헤더
    private JComponent buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0, BRAND, 0,getHeight(), BRAND_DARK));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 72));

        JLabel title = new JLabel("Star Bokchi", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("회원가입", SwingConstants.CENTER);
        subtitle.setForeground(new Color(255,255,255,220));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(Box.createVerticalStrut(10));
        box.add(title);
        box.add(Box.createVerticalStrut(2));
        box.add(subtitle);
        box.add(Box.createVerticalGlue());

        header.add(box, BorderLayout.CENTER);
        return header;
    }

    //중앙
    private JComponent buildCard() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 그림자
                g2.setColor(new Color(0,0,0,25));
                g2.fillRoundRect(6, 8, getWidth()-12, getHeight()-12, 20, 20);

                // 본체
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-12, getHeight()-12, 20, 20);
                g2.dispose();

                super.paintComponent(g);
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;

        JLabel head = new JLabel("회원 정보 입력");
        head.setFont(head.getFont().deriveFont(Font.BOLD, 18f));
        head.setForeground(BRAND);
        card.add(head, c);

        // 필드 보더 공통
        Border fieldBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD)),
                BorderFactory.createEmptyBorder(8,10,8,10)
        );

        // 아이디
        c.gridy++; c.gridwidth = 1;
        card.add(fieldLabel("아이디"), c);
        c.gridx = 1;
        tfUsername.setToolTipText("예: bokchi123");
        tfUsername.setBorder(fieldBorder);
        
        JPanel idRow = new JPanel(new BorderLayout(6, 0));
        idRow.setOpaque(false);
        idRow.add(tfUsername, BorderLayout.CENTER);
        idRow.add(btnCheckId, BorderLayout.EAST);
        card.add(idRow, c);

        // 비밀번호
        c.gridx = 0; c.gridy++;
        card.add(fieldLabel("비밀번호"), c);
        c.gridx = 1;
        pfPassword.setBorder(fieldBorder);
        pfPassword.setEchoChar('\u2022');
        card.add(pfPassword, c);

        // 비밀번호 확인 + 표시 체크박스
        c.gridx = 0; c.gridy++;
        card.add(fieldLabel("비밀번호 확인"), c);
        c.gridx = 1;
        pfPassword2.setBorder(fieldBorder);
        pfPassword2.setEchoChar('\u2022');
        card.add(pfPassword2, c);

        c.gridx = 1; c.gridy++;
        cbShowPw.setOpaque(false);
        cbShowPw.addActionListener(e -> {
            char echo = cbShowPw.isSelected() ? (char)0 : '\u2022';
            pfPassword.setEchoChar(echo);
            pfPassword2.setEchoChar(echo);
        });
        card.add(cbShowPw, c);

        // 이름
        c.gridx = 0; c.gridy++;
        card.add(fieldLabel("이름"), c);
        c.gridx = 1;
        tfName.setBorder(fieldBorder);
        card.add(tfName, c);

        // 전화번호
        c.gridx = 0; c.gridy++;
        card.add(fieldLabel("전화번호"), c);
        c.gridx = 1;
        tfPhone.setToolTipText("예: 010-1234-5678 또는 01012345678");
        tfPhone.setBorder(fieldBorder);
        
        JPanel phoneRow = new JPanel(new BorderLayout(6, 0));
        phoneRow.setOpaque(false);
        phoneRow.add(tfPhone, BorderLayout.CENTER);
        phoneRow.add(btnCheckPhone, BorderLayout.EAST);
        card.add(phoneRow, c);

        // 버튼 행
        c.gridx = 0; c.gridy++;
        c.gridwidth = 2;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        styleGhost(btnCancel);
        stylePrimary(btnSignUp);
        buttons.add(btnCancel);
        buttons.add(btnSignUp);

        card.add(Box.createVerticalStrut(8), c);
        c.gridy++;
        card.add(buttons, c);

        wrap.add(card);
        return wrap;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 13f));
        return l;
    }

    private void stylePrimary(JButton b) {
        b.setBackground(BRAND);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
    }
    private void styleGhost(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(BRAND);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(BRAND));
    }

    private void wireEvents() {
        btnSignUp.addActionListener(this::onSignUp);
        btnCancel.addActionListener(e -> dispose());
        
        btnCheckId.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디를 입력하세요.");
                tfUsername.requestFocus();
                return;
            }
            try {
                var dao = JdbcUserDaoImple.getInstance();
                var exists = dao.findByUsername(username) != null; // existsByUsername 있으면 그걸 써도 OK
                if (exists) {
                    JOptionPane.showMessageDialog(this, "이미 사용 중인 아이디입니다.");
                    tfUsername.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(this, "사용 가능한 아이디입니다.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "중복체크 오류: " + ex.getMessage());
            }
        });

        btnCheckPhone.addActionListener(e -> {
            String phone = tfPhone.getText().trim();
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "전화번호를 입력하세요.");
                tfPhone.requestFocus();
                return;
            }
            // 형식 안내
            if (!phone.matches("\\d{2,3}-\\d{3,4}-\\d{4}") && !phone.matches("\\d{10,11}")) {
                int ans = JOptionPane.showConfirmDialog(this, "전화번호 형식이 일반적이지 않습니다. 계속 확인할까요?",
                        "확인", JOptionPane.YES_NO_OPTION);
                if (ans != JOptionPane.YES_OPTION) return;
            }
            try {
                var dao = JdbcUserDaoImple.getInstance();
                var dup = dao.findByPhone(phone) != null; // existsByPhone(...)이 있으면 그걸 써도 OK
                if (dup) {
                    JOptionPane.showMessageDialog(this, "이미 등록된 전화번호입니다.");
                    tfPhone.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(this, "사용 가능한 전화번호입니다.");
                }
            } catch (NoSuchMethodError err) {
                JOptionPane.showMessageDialog(this, "전화번호 조회 메서드가 없습니다. JdbcUserDaoImple에 findByPhone(String)을 추가하세요.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "중복체크 오류: " + ex.getMessage());
            }
        });
    }

    // 가입
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