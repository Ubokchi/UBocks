package bokchi.java.ui.staff;

import bokchi.java.model.UserVO;

import javax.swing.*;
import java.awt.*;

public class StaffMainFrame extends JFrame {
    private final UserVO staff;

    public StaffMainFrame(UserVO staff) {
        super("STAFF 메인");
        this.staff = staff;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        var tab = new JTabbedPane();
        tab.addTab("상품관리", new JLabel("TODO: ItemManagePanel"));
        tab.addTab("주문조회", new JLabel("TODO: OrderListPanel"));
        tab.addTab("리워드이력(선택)", new JLabel("TODO: RewardHistoryPanel"));

        // 상단 패널
        var top = new JPanel(new BorderLayout());
        top.add(new JLabel("로그인: " + staff.getName() + " (" + staff.getUsername() + ")"), BorderLayout.WEST);

        JButton btnLogout = new JButton("로그아웃");
        btnLogout.addActionListener(e -> {
            dispose(); // 현재 창 닫기
            SwingUtilities.invokeLater(() -> new StaffLoginFrame().setVisible(true));
        });
        top.add(btnLogout, BorderLayout.EAST);

        var root = new JPanel(new BorderLayout());
        root.add(top, BorderLayout.NORTH);
        root.add(tab, BorderLayout.CENTER);
        setContentPane(root);
    }
}