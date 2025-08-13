package bokchi.java.ui.user;

import bokchi.java.model.UserVO;
import bokchi.java.dao.jdbc.JdbcItemDaoImple;
import bokchi.java.model.enums.ItemType;

import javax.swing.*;
import java.awt.*;

public class UserMainFrame extends JFrame {
    private final UserVO customer;

    // 테마 컬러 (스타벅스 그린 톤)
    private static final Color BRAND = new Color(0x006241);
    private static final Color BG = new Color(0xF7F7F7);

    private final JTextField tfSearch = new JTextField(20);
    private final JToggleButton btnDrink = new JToggleButton("DRINK");
    private final JToggleButton btnFood  = new JToggleButton("FOOD");
    private final JToggleButton btnGoods = new JToggleButton("GOODS");

    private final ItemGridPanel gridPanel = new ItemGridPanel();
    private final CartPanel cartPanel;

    // 현재 선택된 카테고리
    private ItemType currentType = ItemType.DRINK;

    public UserMainFrame(UserVO customer) {
        super("주문 · 리워드");
        this.customer = customer;
        this.cartPanel = new CartPanel(customer);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        setContentPane(buildRoot());

        // 초기 로드
        loadFromDao(currentType);
        
        cartPanel.setCheckoutListener((cust, lines, total, usedFreeDrink, freeDrinkItemId) -> {
            var service = new bokchi.java.service.JdbcCheckoutService();
            try {
                var result = service.checkout(cust, lines, total, usedFreeDrink, freeDrinkItemId);
                JOptionPane.showMessageDialog(this,
                    "결제가 완료되었습니다.\n주문번호: " + result.orderId() +
                    "\n합계: " + result.totalAmount() + "원\n적립 스탬프: " + result.stampsEarned() + "개" +
                    (result.usedFreeDrink() ? "\n무료 음료 1잔 사용" : "")
                );

                // 성공 시 장바구니 비우기
                cartPanel.clearCart();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "결제 실패: " + ex.getMessage());
                // 실패 시 장바구니는 유지
            }
        });
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildLeftSidebar(), BorderLayout.WEST);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(cartPanel, BorderLayout.EAST);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E5E5)),
                BorderFactory.createEmptyBorder(10,12,10,12)
        ));

        // 좌측: 로고/타이틀
        JLabel title = new JLabel("Star Bokchi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(BRAND);

        // 가운데: 검색
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton btnSearch = new JButton("검색");
        center.setOpaque(false);
        center.add(tfSearch);
        center.add(btnSearch);

        // 우측: 사용자/로그아웃
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel lbUser = new JLabel(customer.getName() + " 님 · 스탬프 " + customer.getRewardBalance());
        JButton btnLogout = new JButton("로그아웃");
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new UserLoginFrame().setVisible(true));
        });
        right.add(lbUser);
        right.add(btnLogout);

        header.add(title, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        // 검색 동작: 비어있으면 현재 카테고리, 아니면 DB 검색
        btnSearch.addActionListener(e -> {
            String q = tfSearch.getText().trim();
            if (q.isEmpty()) {
                loadFromDao(currentType);
            } else {
                searchFromDao(q);
            }
        });

        return header;
    }

    private JComponent buildLeftSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Color.WHITE);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E5E5)),
                BorderFactory.createEmptyBorder(12,12,12,12)
        ));
        JLabel cat = new JLabel("카테고리");
        cat.setFont(cat.getFont().deriveFont(Font.BOLD));
        cat.setForeground(BRAND);

        ButtonGroup g = new ButtonGroup();
        btnDrink.setSelected(true);
        for (JToggleButton b : new JToggleButton[]{btnDrink, btnFood, btnGoods}) {
            b.setFocusPainted(false);
            b.setBackground(Color.WHITE);
            b.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            g.add(b);
        }

        // 카테고리 변경 시 DB에서 다시 로드
        btnDrink.addActionListener(e -> { currentType = ItemType.DRINK; loadFromDao(currentType); });
        btnFood.addActionListener(e -> { currentType = ItemType.FOOD;  loadFromDao(currentType); });
        btnGoods.addActionListener(e -> { currentType = ItemType.GOODS; loadFromDao(currentType); });

        side.add(cat);
        side.add(Box.createVerticalStrut(8));
        side.add(btnDrink);
        side.add(Box.createVerticalStrut(4));
        side.add(btnFood);
        side.add(Box.createVerticalStrut(4));
        side.add(btnGoods);
        side.add(Box.createVerticalGlue());
        side.setPreferredSize(new Dimension(160, 0));
        return side;
    }

    private JComponent buildCenter() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.add(gridPanel, BorderLayout.CENTER);
        return wrap;
    }

    // DB에서 현재 카테고리로 읽어서 카드 구성 (없으면 안내)
    private void loadFromDao(ItemType type) {
        gridPanel.clear();
        var itemDao = JdbcItemDaoImple.getInstance();
        var items = itemDao.findActiveByType(type);

        if (items.isEmpty()) {
            gridPanel.addCard(new MessageCard("상품이 없습니다."));
        } else {
            for (var vo : items) {
                gridPanel.addCard(new ItemCard(vo, e -> cartPanel.addItem(vo, 1)));
            }
        }
        gridPanel.refresh();
    }

    // DB에서 검색(판매중 + 이름 LIKE, 타입 무관) — 없으면 안내
    private void searchFromDao(String keyword) {
        gridPanel.clear();
        var itemDao = JdbcItemDaoImple.getInstance();
        var items = itemDao.searchActiveByText(keyword);

        if (items.isEmpty()) {
            gridPanel.addCard(new MessageCard("검색 결과가 없습니다."));
        } else {
            for (var vo : items) {
                gridPanel.addCard(new ItemCard(vo, e -> cartPanel.addItem(vo, 1)));
            }
        }
        gridPanel.refresh();
    }
}