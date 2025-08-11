package bokchi.java.ui.user;

import bokchi.java.model.UserVO;

import javax.swing.*;
import java.awt.*;

public class UserMainFrame extends JFrame {
    private final UserVO customer;

    public UserMainFrame(UserVO customer) {
        super("주문 · 리워드");
        this.customer = customer;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        // 좌: 카테고리/필터
        JPanel left = new JPanel(new GridLayout(0, 1, 8, 8));
        left.setBorder(BorderFactory.createTitledBorder("카테고리"));
        left.add(new JButton("DRINK"));
        left.add(new JButton("FOOD"));
        left.add(new JButton("GOODS"));

        // 중간: 상품 목록
        JPanel center = new JPanel(new BorderLayout(8,8));
        center.setBorder(BorderFactory.createTitledBorder("상품 목록"));
        center.add(new JScrollPane(new JList<>(new String[]{"(예) 아메리카노", "(예) 카페라떼"})), BorderLayout.CENTER);
        JButton btnAddToCart = new JButton("장바구니 담기");
        center.add(btnAddToCart, BorderLayout.SOUTH);

        // 우: 장바구니
        CartPanel cartPanel = new CartPanel(customer);

        // 상단: 계정정보 + 로그아웃
        JLabel lbUser = new JLabel("고객: " + customer.getName() + " / 스탬프: " + customer.getRewardBalance());
        JButton btnLogout = new JButton("로그아웃");
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new UserLoginFrame().setVisible(true));
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(lbUser, BorderLayout.WEST);
        top.add(btnLogout, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.add(top, BorderLayout.NORTH);
        root.add(left, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);
        root.add(cartPanel, BorderLayout.EAST);
        setContentPane(root);
    }
}