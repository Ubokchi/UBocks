package bokchi.java.ui.user;

import bokchi.java.model.ItemVO;
import bokchi.java.model.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CartPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;
    private final JLabel lbTotal = new JLabel("총 금액: 0원");

    private final JButton btnRemove = new JButton("선택 삭제");
    private final JButton btnClear  = new JButton("비우기");
    private final JButton btnCheckout = new JButton("결제");

    private final UserVO customer;
    private CheckoutListener checkoutListener; // 결제

    public CartPanel(UserVO customer) {
        this.customer = customer;

        setLayout(new BorderLayout(8,8));
        setPreferredSize(new Dimension(320, 0));
        setBorder(BorderFactory.createTitledBorder("장바구니"));

        model = new DefaultTableModel(new Object[]{"상품명", "수량", "금액", "itemId", "단가", "재고"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 1; } // 수량만 수정
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 1,2,4,5 -> Integer.class; // 수량/금액/단가/재고
                    default -> String.class;       // 상품명
                };
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);

        // 숨김 컬럼 (itemId, 단가, 재고)
        hideColumn(3);
        hideColumn(4);
        hideColumn(5);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // 합계 (첫 줄)
        lbTotal.setFont(lbTotal.getFont().deriveFont(Font.BOLD, 14f));
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.add(lbTotal);

        // 버튼 3개 (둘째 줄)
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.add(btnRemove);
        btns.add(btnClear);
        btns.add(btnCheckout);

        // 두 줄로 아래 배치
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(totalPanel);
        bottom.add(btns);
        add(bottom, BorderLayout.SOUTH);

        // 수량 변경 시 재계산
        model.addTableModelListener(e -> {
            if (e.getColumn() == 1 || e.getColumn() == -1) {
                enforceStockAndRecalc();
            }
        });

        // 버튼 동작
        btnRemove.addActionListener(e -> removeSelected());
        btnClear.addActionListener(e -> clearAll());
        btnCheckout.addActionListener(e -> onCheckout());
    }

    // 결제 연결
    public void setCheckoutListener(CheckoutListener listener) {
        this.checkoutListener = listener;
    }

    // 숨기기
    private void hideColumn(int index) {
        table.getColumnModel().getColumn(index).setMinWidth(0);
        table.getColumnModel().getColumn(index).setMaxWidth(0);
        table.getColumnModel().getColumn(index).setWidth(0);
    }

    // 장바구니 추가
    public void addItem(ItemVO vo, int qty) {
        if (!vo.isActive()) { JOptionPane.showMessageDialog(this, "판매 중이 아닌 상품입니다."); return; }
        if (vo.getStock() <= 0) { JOptionPane.showMessageDialog(this, "재고가 없습니다."); return; }

        // 같은 itemId 있으면 수량 합치기
        for (int r = 0; r < model.getRowCount(); r++) {
            int existingId = (Integer) model.getValueAt(r, 3);
            if (existingId == vo.getItemId()) {
                int stock = (Integer) model.getValueAt(r, 5);
                int curQty = (Integer) model.getValueAt(r, 1);
                int newQty = Math.min(curQty + qty, stock);
                if (newQty == curQty) {
                    JOptionPane.showMessageDialog(this, "재고 한도를 초과할 수 없습니다.");
                    return;
                }
                model.setValueAt(newQty, r, 1);
                model.setValueAt(newQty * (Integer) model.getValueAt(r, 4), r, 2);
                recalcTotal();
                return;
            }
        }

        // 새 행 추가
        int addQty = Math.min(qty, vo.getStock());
        model.addRow(new Object[]{
                vo.getName(),            // 0: 상품명
                addQty,                  // 1: 수량
                addQty * vo.getPrice(),  // 2: 금액
                vo.getItemId(),          // 3: itemId(숨김)
                vo.getPrice(),           // 4: 단가(숨김)
                vo.getStock()            // 5: 재고(숨김)
        });
        recalcTotal();
    }

    // 선택 행 삭제
    public void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
            return;
        }
        model.removeRow(row);
        recalcTotal();
    }

    // 전체 비우기 + 다이얼로ㄱ,
    public void clearAll() {
        if (model.getRowCount() == 0) return;
        int ok = JOptionPane.showConfirmDialog(this, "장바구니를 모두 비울까요?", "확인", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            model.setRowCount(0);
            recalcTotal();
        }
    }

    //바로 비우기
    public void clearCart() {
        model.setRowCount(0);
        recalcTotal();
    }

    // 재고 한도
    private void enforceStockAndRecalc() {
        for (int r = 0; r < model.getRowCount(); r++) {
            int qty   = (Integer) model.getValueAt(r, 1);
            int unit  = (Integer) model.getValueAt(r, 4);
            int stock = (Integer) model.getValueAt(r, 5);

            if (qty > stock) {
                qty = stock;
                model.setValueAt(qty, r, 1);
                JOptionPane.showMessageDialog(this, "재고 한도를 초과할 수 없습니다.");
            } else if (qty <= 0) {
                qty = 1;
                model.setValueAt(qty, r, 1);
            }
            model.setValueAt(qty * unit, r, 2);
        }
        recalcTotal();
    }

    // 총 금액 계산
    private void recalcTotal() {
        lbTotal.setText("총 금액: " + getTotal() + "원");
    }

    // 현재 합계 라인
    public int getTotal() {
        int total = 0;
        for (int r = 0; r < model.getRowCount(); r++) {
            total += (Integer) model.getValueAt(r, 2);
        }
        return total;
    }

    // 스냡샷 라인 추출
    public List<CartLine> snapshotLines() {
        List<CartLine> lines = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            String name  = (String)  model.getValueAt(r, 0);
            int qty      = (Integer) model.getValueAt(r, 1);
            int amount   = (Integer) model.getValueAt(r, 2);
            int itemId   = (Integer) model.getValueAt(r, 3);
            int unit     = (Integer) model.getValueAt(r, 4);
            lines.add(new CartLine(itemId, name, qty, unit, amount));
        }
        return lines;
    }

    // 결제 버튼 - UI에서 무료쿠폰 적용/확인만 처리
    private void onCheckout() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
            return;
        }

        // 무료 1잔 사용 여부 및 대상 선택 (UI 로직만)
        final int COST = 12;
        boolean usedFreeDrink = false;
        Integer freeDrinkItemId = null;

        if (customer != null && customer.getRewardBalance() >= COST) {
            var lines = snapshotLines();
            RewardChoiceDialog dlg = new RewardChoiceDialog(
                    SwingUtilities.getWindowAncestor(this),
                    lines,
                    customer.getRewardBalance(),
                    COST
            );
            boolean use = dlg.showDialog();
            if (use) {
                freeDrinkItemId = dlg.getSelectedItemId();
                if (freeDrinkItemId == null) {
                    JOptionPane.showMessageDialog(this, "무료 적용 대상을 선택하지 않았습니다.");
                } else {
                    // 화면 표시용: 장바구니에 0원 라인 추가(결제 금액/합계 갱신)
                    if (applyFreeToCart(freeDrinkItemId)) {
                        usedFreeDrink = true;
                    } else {
                        JOptionPane.showMessageDialog(this, "선택한 상품을 장바구니에서 찾지 못했거나 적용할 수 없습니다.");
                    }
                }
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this, "결제를 진행하시겠습니까?", "결제 확인", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (checkoutListener == null) {
            JOptionPane.showMessageDialog(this, "결제 로직이 연결되어 있지 않습니다.\nsetCheckoutListener(...)를 설정하세요.");
            return;
        }

        // DB/트랜잭션은 외부로 위임
        checkoutListener.onCheckout(customer, snapshotLines(), getTotal(), usedFreeDrink, freeDrinkItemId);

        // 성공 시 장바구니 비우기(외부에서 성공여부 판단 후 호출하고 싶으면 clearCart()를 밖에서 호출해도 됨)
        clearCart();
    }

    // 장바구니에서 itemId 대상 라인에 '1잔 무료' 적용: 원 라인 qty-1, 0원 라인 +1 추가
    private boolean applyFreeToCart(int itemId) {
        for (int r = 0; r < model.getRowCount(); r++) {
            int rid = (Integer) model.getValueAt(r, 3);   // itemId
            int qty = (Integer) model.getValueAt(r, 1);
            int unit= (Integer) model.getValueAt(r, 4);

            if (rid == itemId && unit > 0 && qty > 0) {
                int newQty = qty - 1;
                model.setValueAt(newQty, r, 1);
                model.setValueAt(newQty * unit, r, 2);

                String name = (String) model.getValueAt(r, 0);
                int stock   = (Integer) model.getValueAt(r, 5);
                model.addRow(new Object[]{
                        name + " (무료)", // 0
                        1,                // 1
                        0,                // 2
                        itemId,           // 3
                        0,                // 4 (unitPrice=0 → 적립 제외)
                        stock             // 5
                });

                if (newQty == 0) model.removeRow(r);
                recalcTotal();
                return true;
            }
        }
        return false;
    }

    // 결제 인터페이스
    public interface CheckoutListener {
        void onCheckout(UserVO customer,
                        List<CartLine> lines,
                        int totalAmount,
                        boolean usedFreeDrink,
                        Integer freeDrinkItemId);
    }
    
    // 스냅샷용
    public static class CartLine {
        public final int itemId;
        public final String name;
        public final int qty;
        public final int unitPrice;  // UI에는 숨김
        public final int lineAmount; // qty * unitPrice

        public CartLine(int itemId, String name, int qty, int unitPrice, int lineAmount) {
            this.itemId = itemId;
            this.name = name;
            this.qty = qty;
            this.unitPrice = unitPrice;
            this.lineAmount = lineAmount;
        }
    }
}