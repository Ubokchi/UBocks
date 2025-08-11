package bokchi.java.ui.user;

import bokchi.java.model.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CartPanel extends JPanel {
    private final UserVO customer;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"상품", "수량", "단가", "금액"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return c == 1; } // 수량만 수정
        @Override public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 1,2,3 -> Integer.class;
                default -> String.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel lbTotal = new JLabel("합계: 0 원");
    private final JButton btnCheckout = new JButton("결제");

    public CartPanel(UserVO customer) {
        super(new BorderLayout(8, 8));
        this.customer = customer;

        setPreferredSize(new Dimension(320, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E5E5)),
                BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        lbTotal.setFont(lbTotal.getFont().deriveFont(Font.BOLD, 14f));
        bottom.add(lbTotal, BorderLayout.WEST);
        bottom.add(btnCheckout, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // 수량 수정 시 합계 재계산
        model.addTableModelListener(e -> recalc());

        btnCheckout.addActionListener(e -> onCheckout());
    }

    public void addItem(String name, int qty, int unit) {
        // 동일 상품이면 수량만 + 처리
        for (int i = 0; i < model.getRowCount(); i++) {
            if (name.equals(model.getValueAt(i, 0))) {
                int newQty = (Integer) model.getValueAt(i, 1) + qty;
                model.setValueAt(newQty, i, 1);
                return;
            }
        }
        model.addRow(new Object[]{name, qty, unit, qty * unit});
        recalc();
    }

    private void recalc() {
        int total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int qty = (Integer) model.getValueAt(i, 1);
            int unit = (Integer) model.getValueAt(i, 2);
            model.setValueAt(qty * unit, i, 3);
            total += qty * unit;
        }
        lbTotal.setText("합계: " + total + " 원");
    }

    private void onCheckout() {
        // TODO: RewardChoiceDialog → OrderService.checkout(...) 연결
        boolean eligible = customer.getRewardBalance() >= 10; // 예시
        boolean useReward = false;
        if (eligible) {
            RewardChoiceDialog dlg = new RewardChoiceDialog(
                    SwingUtilities.getWindowAncestor(this), customer.getRewardBalance(), 10);
            useReward = dlg.showDialog();
        }
        JOptionPane.showMessageDialog(this, "결제 처리 TODO (보상사용=" + useReward + ")");
    }
}