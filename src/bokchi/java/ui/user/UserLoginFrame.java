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
    private static final int LONG_PRESS_MS = 3000; // 3초

    // 브랜드 컬러
    private static final Color BRAND = new Color(0x006241);
    private static final Color BRAND_DARK = new Color(0x004D33);
    private static final Color BG = new Color(0xF6F7F6);

    public UserLoginFrame() {
        super("Star Bokchi · 로그인");
        setDefaultCloseOperation(EXIT_ON_CLOSE); // 창을 닫을 때 프로그램 종료
        setSize(480, 520);
        setLocationRelativeTo(null);	// 화면 정중앙에 띄움

        setContentPane(buildRoot());
        installSecretSwitchOverlay();	// 3초 롱프레스 비밀 스위치

        btnLogin.addActionListener(this::onLogin);	// 로그인 버튼 누르면 onLogin 실행
        btnSignUp.addActionListener(e ->
                SwingUtilities.invokeLater(() -> new UserSignUpFrame().setVisible(true))
        );	// 회원가입 버튼 누르면 회원가입 창 띄움
        getRootPane().setDefaultButton(btnLogin);	// 엔터 키를 누르면 로그인
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
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,25));
                g2.fillRoundRect(6, 8, getWidth()-12, getHeight()-12, 20, 20);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-12, getHeight()-12, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        // 타이틀
        JLabel lb = new JLabel("고객 로그인");
        lb.setFont(lb.getFont().deriveFont(Font.BOLD, 18f));
        lb.setForeground(new Color(0x006241));
        gbcAdd(card, lb, 0, 0, 2);

        // 아이디
        gbcAdd(card, fieldLabel("아이디"), 0, 1, 1);
        tfUsername.setToolTipText("예: bokchi123");
        tfUsername.setBorder(compoundFieldBorder());
        gbcAdd(card, tfUsername, 1, 1, 1);

        // 비밀번호
        gbcAdd(card, fieldLabel("비밀번호"), 0, 2, 1);
        pfPassword.setToolTipText("비밀번호를 입력하세요");
        pfPassword.setBorder(compoundFieldBorder());
        pfPassword.setEchoChar('\u2022');
        gbcAdd(card, pfPassword, 1, 2, 1);

        // 비밀번호 표시
        cbShowPw.setOpaque(false);
        cbShowPw.addActionListener(e -> pfPassword.setEchoChar(cbShowPw.isSelected() ? (char)0 : '\u2022'));
        gbcAdd(card, cbShowPw, 1, 3, 1);

        // 버튼 행
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        styleGhost(btnSignUp);
        stylePrimary(btnLogin);
        btnRow.add(btnSignUp);
        btnRow.add(btnLogin);

        // 여백 + 버튼
        gbcAdd(card, Box.createVerticalStrut(6), 0, 4, 2);
        gbcAdd(card, btnRow, 0, 5, 2);

        wrap.add(card);
        return wrap;
    }
    
    private void gbcAdd(JPanel parent, Component comp, int x, int y, int w) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = w;
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        parent.add(comp, gc);
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

    // 로그인
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

    // 스태프 전환
    private void installSecretSwitchOverlay() {
        JRootPane rp = getRootPane();

        // 히트박스 영역만 이벤트 받는 곳
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