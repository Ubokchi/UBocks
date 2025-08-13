package bokchi.java.ui.user;

import bokchi.java.dao.jdbc.JdbcUserDaoImple;
import bokchi.java.model.UserVO;
import bokchi.java.model.enums.Role;
import bokchi.java.ui.staff.StaffLoginFrame;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserLoginFrame extends JFrame {
    private final JTextField tfUsername = new JTextField(18);
    private final JPasswordField pfPassword = new JPasswordField(18);
    private final JCheckBox cbShowPw = new JCheckBox("비밀번호 표시");
    private final JButton btnLogin = new JButton("로그인");
    private final JButton btnSignUp = new JButton("회원가입");

    // 비밀 전환용
    private static final int HITBOX = 48;          // 오른쪽-위 48px 정사각형
    private static final int LONG_PRESS_MS = 5000; // 5초

    // 브랜드 컬러
    private static final Color BRAND = new Color(0x006241);
    private static final Color BRAND_DARK = new Color(0x004D33);
    private static final Color BG = new Color(0xF6F7F6);

    public UserLoginFrame() {
        super("Bokchi Coffee · 로그인");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);

        setContentPane(buildRoot());
        installSecretSwitchOverlay();

        btnLogin.addActionListener(this::onLogin);
        btnSignUp.addActionListener(e ->
                SwingUtilities.invokeLater(() -> new UserSignUpFrame().setVisible(true))
        );
        getRootPane().setDefaultButton(btnLogin);
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCard(), BorderLayout.CENTER);
        return root;
    }

    /** 상단 헤더(브랜드 바) */
    private JComponent buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 그라데이션 살짝
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
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JLabel subtitle = new JLabel("주문 · 리워드", SwingConstants.CENTER);
        subtitle.setForeground(new Color(255,255,255,210));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(Box.createVerticalStrut(16));
        box.add(title);
        box.add(Box.createVerticalStrut(4));
        box.add(subtitle);
        box.add(Box.createVerticalGlue());

        header.add(box, BorderLayout.CENTER);
        return header;
    }

    /** 가운데 카드(라운드 컨테이너) */
    private JComponent buildCard() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                // 부드러운 라운드 카드 + 그림자 느낌
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 그림자
                g2.setColor(new Color(0,0,0,25));
                g2.fillRoundRect(6, 8, getWidth()-12, getHeight()-12, 20, 20);

                // 바탕
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-12, getHeight()-12, 20, 20);
                g2.dispose();

                super.paintComponent(g);
            }

            @Override public boolean isOpaque() { return false; }
        };
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;

        // 카드 타이틀
        JLabel lb = new JLabel("고객 로그인");
        lb.setFont(lb.getFont().deriveFont(Font.BOLD, 18f));
        lb.setForeground(BRAND);
        card.add(lb, c);

        // username
        c.gridy++;
        c.gridwidth = 1;
        card.add(fieldLabel("아이디"), c);
        c.gridx = 1;
        tfUsername.setToolTipText("예: bokchi123");
        tfUsername.setBorder(compoundFieldBorder());
        card.add(tfUsername, c);

        // password
        c.gridx = 0; c.gridy++;
        card.add(fieldLabel("비밀번호"), c);
        c.gridx = 1;
        pfPassword.setToolTipText("비밀번호를 입력하세요");
        pfPassword.setBorder(compoundFieldBorder());
        card.add(pfPassword, c);

        // show password
        c.gridx = 1; c.gridy++;
        cbShowPw.setOpaque(false);
        cbShowPw.addActionListener(e -> pfPassword.setEchoChar(cbShowPw.isSelected() ? (char)0 : '\u2022'));
        // 초기엔 숨김
        pfPassword.setEchoChar('\u2022');
        card.add(cbShowPw, c);

        // 버튼들
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stylePrimary(btnLogin);
        styleGhost(btnSignUp);

        btnRow.setOpaque(false);
        btnRow.add(btnSignUp);
        btnRow.add(btnLogin);

        c.gridx = 0; c.gridy++;
        c.gridwidth = 2;
        card.add(Box.createVerticalStrut(6), c);
        c.gridy++;
        card.add(btnRow, c);

        wrap.add(card);
        return wrap;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 13f));
        return l;
    }

    private Border compoundFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDDDDD)),
                BorderFactory.createEmptyBorder(8,10,8,10)
        );
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

    // ================= 로그인 로직 =================
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

    // =============== 스태프 전환(오른쪽 위 5초 롱프레스) ===============
    private void installSecretSwitchOverlay() {
        JRootPane rp = getRootPane();

        // 히트박스 영역만 이벤트 받는 글래스페인
        JComponent glass = new JComponent() {
            @Override public boolean isOpaque() { return false; }
            @Override public boolean contains(int x, int y) {
                int w = getWidth();
                int xFromRight = w - x;
                return (xFromRight >= 0 && xFromRight <= HITBOX) && (y >= 0 && y <= HITBOX);
            }
        };
        rp.setGlassPane(glass);
        glass.setVisible(true);

        final Timer[] holdTimer = new Timer[1];
        glass.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                holdTimer[0] = new Timer(LONG_PRESS_MS, ev -> switchToStaffLogin());
                holdTimer[0].setRepeats(false);
                holdTimer[0].start();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (holdTimer[0] != null && holdTimer[0].isRunning()) {
                    holdTimer[0].stop();
                }
            }
        });

    }

    private void switchToStaffLogin() {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new StaffLoginFrame().setVisible(true);
        });
    }
} 