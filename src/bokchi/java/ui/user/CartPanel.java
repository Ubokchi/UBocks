package bokchi.java.ui.user;

import bokchi.java.model.ItemVO;
import bokchi.java.model.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CartPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;
    private final JLabel lbTotal = new JLabel("총 금액: 0원");

    private final UserVO customer; // ← 로그인 사용자 보관(필요 시 사용)

    public CartPanel(UserVO customer) {
        this.customer = customer;

        setLayout(new BorderLayout(8,8));
        setPreferredSize(new Dimension(320, 0));
        setBorder(BorderFactory.createTitledBorder("장바구니"));

        model = new DefaultTableModel(new Object[]{"상품명", "수량", "금액", "itemId", "단가", "재고"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // 상품명/금액/숨김컬럼은 수정 불가, 수량만 수정 가능
                return col == 1;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 1, 2, 4, 5 -> Integer.class; // 수량/금액/단가/재고는 정수
                    default -> String.class;          // 상품명 등
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
        add(lbTotal, BorderLayout.SOUTH);

        // 수량 셀 변경 시 재고 한도 적용 + 금액/합계 재계산
        model.addTableModelListener(e -> {
            if (e.getColumn() == 1 || e.getColumn() == -1) {
                enforceStockAndRecalc();
            }
        });
    }

    /** 컬럼 숨기기 */
    private void hideColumn(int index) {
        table.getColumnModel().getColumn(index).setMinWidth(0);
        table.getColumnModel().getColumn(index).setMaxWidth(0);
        table.getColumnModel().getColumn(index).setWidth(0);
    }

    /** 장바구니에 상품 추가 */
    public void addItem(ItemVO vo, int qty) {
        if (!vo.isActive()) {
            JOptionPane.showMessageDialog(this, "판매 중이 아닌 상품입니다.");
            return;
        }
        if (vo.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "재고가 없습니다.");
            return;
        }

        // 이미 있는 상품인지 확인(같은 itemId는 수량 합치기)
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

        // 새 상품 추가
        int addQty = Math.min(qty, vo.getStock());
        model.addRow(new Object[]{
                vo.getName(),            // 0: 상품명
                addQty,                  // 1: 수량
                addQty * vo.getPrice(),  // 2: 금액
                vo.getItemId(),          // 3: itemId (숨김)
                vo.getPrice(),           // 4: 단가 (숨김)
                vo.getStock()            // 5: 재고 (숨김)
        });
        recalcTotal();
    }

    /** 수량 변경 시 재고 한도 강제 및 금액/합계 재계산 */
    private void enforceStockAndRecalc() {
        for (int r = 0; r < model.getRowCount(); r++) {
            int qty = (Integer) model.getValueAt(r, 1);
            int unit = (Integer) model.getValueAt(r, 4);
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

    /** 총 금액 다시 계산 */
    private void recalcTotal() {
        int total = 0;
        for (int r = 0; r < model.getRowCount(); r++) {
            total += (Integer) model.getValueAt(r, 2);
        }
        lbTotal.setText("총 금액: " + total + "원");
    }

    /** 선택 행 삭제(옵션) */
    public void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        model.removeRow(row);
        recalcTotal();
    }
}