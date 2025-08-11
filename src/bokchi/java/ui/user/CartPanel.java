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
    };
    private final JTable table = new JTable(model);
    private final JLabel lbTotal = new JLabel("합계: 0 원");
    private final JButton btnCheckout = new JButton("결제");

    public CartPanel(UserVO customer) {
        super(new BorderLayout(8, 8));
        this.customer = customer;
        setBorder(BorderFactory.createTitledBorder("장바구니"));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(lbTotal, BorderLayout.WEST);
        bottom.add(btnCheckout, BorderLayout.EAST);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnCheckout.addActionListener(e -> onCheckout());
    }

    // TODO: 서비스 붙일 때 실제 ItemVO, 가격 계산 로직 연결
    public void addDummyRow(String name, int qty, int unit) {
        int amount = qty * unit;
        model.addRow(new Object[]{name, qty, unit, amount});
        recalc();
    }

    private void recalc() {
        int total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int qty = (int) model.getValueAt(i, 1);
            int unit = (int) model.getValueAt(i, 2);
            model.setValueAt(qty * unit, i, 3);
            total += qty * unit;
        }
        lbTotal.setText("합계: " + total + " 원");
    }

    private void onCheckout() {
        // 1) 보상 사용 여부 묻기 (임계치 넘으면)
        boolean eligible = customer.getRewardBalance() >= 10; // THRESHOLD=10 가정
        boolean useReward = false;
        if (eligible) {
            RewardChoiceDialog dlg = new RewardChoiceDialog(SwingUtilities.getWindowAncestor(this), customer.getRewardBalance(), 10);
            useReward = dlg.showDialog();
        }
        // 2) OrderService.checkout(...) 호출 예정 (TODO)
        JOptionPane.showMessageDialog(this, "결제 처리 TODO (보상사용=" + useReward + ")");
    }
}