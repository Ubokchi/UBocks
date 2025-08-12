package bokchi.java.ui.user;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.model.ItemVO;
import bokchi.java.model.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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
	private CheckoutListener checkoutListener; // 결제 콜백

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

		// 하단: 합계 (첫 줄)
		lbTotal.setFont(lbTotal.getFont().deriveFont(Font.BOLD, 14f));
		JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		totalPanel.add(lbTotal);

		// 버튼 3개 (둘째 줄)
		JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		btns.add(btnRemove);
		btns.add(btnClear);
		btns.add(btnCheckout);

		// bottom 패널에 두 줄로 배치
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
		bottom.add(totalPanel);
		bottom.add(btns);

		add(bottom, BorderLayout.SOUTH);

		// 수량 변경 시 재고/금액/합계 반영
		model.addTableModelListener(e -> {
			if (e.getColumn() == 1 || e.getColumn() == -1) {
				enforceStockAndRecalc();
			}
		});

		// 버튼 동작
		btnRemove.addActionListener(e -> removeSelected());
		btnClear.addActionListener(e -> clearAll());
		btnCheckout.addActionListener(e -> {
		    // 0) 장바구니 비었는지 확인
		    if (model.getRowCount() == 0) {
		        JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
		        return;
		    }

		    int confirm = JOptionPane.showConfirmDialog(
		            this, "결제를 진행하시겠습니까?", "결제 확인", JOptionPane.YES_NO_OPTION
		    );
		    if (confirm != JOptionPane.YES_OPTION) return;

		    Connection conn = null;
		    PreparedStatement psOrder = null;
		    PreparedStatement psOrderItem = null;
		    PreparedStatement psUpdateStock = null;
		    PreparedStatement psStampEligible = null;
		    PreparedStatement psUpdateUserReward = null;
		    ResultSet rsKeys = null;
		    ResultSet rs = null;

		    try {
		        // 1) 트랜잭션 시작
		        conn = ConnectionProvider.getConnection();
		        conn.setAutoCommit(false);

		        // 2) 합계 계산
		        int total = 0;
		        for (int r = 0; r < model.getRowCount(); r++) {
		            total += (Integer) model.getValueAt(r, 2); // 금액 컬럼
		        }

		        // 3) orders INSERT (PENDING)
		        String sqlOrder = "INSERT INTO orders (user_id, status, total_amount) VALUES (?, 'PENDING', ?)";
		        psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
		        if (customer == null || customer.getUserId() == 0) psOrder.setNull(1, Types.INTEGER);
		        else psOrder.setInt(1, customer.getUserId());
		        psOrder.setInt(2, total);
		        int ins = psOrder.executeUpdate();
		        if (ins != 1) throw new SQLException("주문 생성 실패");

		        rsKeys = psOrder.getGeneratedKeys();
		        if (!rsKeys.next()) throw new SQLException("order_id 생성 실패");
		        int orderId = rsKeys.getInt(1);

		        // 4) 준비된 쿼리들
		        String sqlOrderItem = "INSERT INTO order_items (order_id, item_id, qty, unit_price) VALUES (?, ?, ?, ?)";
		        String sqlUpdateStock = "UPDATE items SET stock = stock - ? WHERE item_id = ? AND stock >= ?";
		        String sqlCheckStamp  = "SELECT stamp_eligible FROM items WHERE item_id = ?";

		        psOrderItem = conn.prepareStatement(sqlOrderItem);
		        psUpdateStock = conn.prepareStatement(sqlUpdateStock);
		        psStampEligible = conn.prepareStatement(sqlCheckStamp);

		        int stampsEarned = 0;

		        // 5) 라인별 insert + 재고 차감 + 스탬프 적립 계산
		        for (int r = 0; r < model.getRowCount(); r++) {
		            int itemId = (Integer) model.getValueAt(r, 3);
		            int qty    = (Integer) model.getValueAt(r, 1);
		            int unit   = (Integer) model.getValueAt(r, 4);

		            // 5-1) order_items INSERT
		            psOrderItem.clearParameters();
		            psOrderItem.setInt(1, orderId);
		            psOrderItem.setInt(2, itemId);
		            psOrderItem.setInt(3, qty);
		            psOrderItem.setInt(4, unit);
		            psOrderItem.executeUpdate();

		            // 5-2) 재고 차감(낙관적 잠금)
		            psUpdateStock.clearParameters();
		            psUpdateStock.setInt(1, qty);
		            psUpdateStock.setInt(2, itemId);
		            psUpdateStock.setInt(3, qty);
		            int updated = psUpdateStock.executeUpdate();
		            if (updated != 1) {
		                throw new SQLException("재고 부족 또는 동시성 충돌: item_id=" + itemId);
		            }

		            // 5-3) 스탬프 적립 여부 확인
		            psStampEligible.clearParameters();
		            psStampEligible.setInt(1, itemId);
		            rs = psStampEligible.executeQuery();
		            boolean eligible = false;
		            if (rs.next()) {
		                eligible = rs.getBoolean(1);
		            }
		            rs.close(); rs = null;

		            if (eligible) {
		                stampsEarned += qty; // 한 잔당 1개 적립 정책
		            }
		        }

		        // 6) 스탬프 적립 반영 (고객 주문인 경우)
		        if (customer != null && customer.getUserId() != 0 && stampsEarned > 0) {
		            String sqlReward = "UPDATE users SET reward_balance = reward_balance + ? WHERE user_id = ?";
		            psUpdateUserReward = conn.prepareStatement(sqlReward);
		            psUpdateUserReward.setInt(1, stampsEarned);
		            psUpdateUserReward.setInt(2, customer.getUserId());
		            psUpdateUserReward.executeUpdate();

		            // (선택) reward_history INSERT도 원하면 여기서 추가
		            // INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'EARN')
		        }

		        // 7) 주문 상태 PAID로 변경
		        String sqlPaid = "UPDATE orders SET status='PAID' WHERE order_id = ?";
		        try (PreparedStatement ps = conn.prepareStatement(sqlPaid)) {
		            ps.setInt(1, orderId);
		            ps.executeUpdate();
		        }

		        // 8) 커밋
		        conn.commit();

		        // 9) 성공 안내 + 장바구니 비우기
		        JOptionPane.showMessageDialog(this,
		                "결제가 완료되었습니다.\n주문번호: " + orderId +
		                "\n합계: " + total + "원\n적립 스탬프: " + stampsEarned + "개");
		        // 장바구니 비우기
		        model.setRowCount(0);
		        lbTotal.setText("총 금액: 0원");

		    } catch (Exception ex) {
		        // 롤백
		        try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
		        ex.printStackTrace();
		        JOptionPane.showMessageDialog(this, "결제 처리 중 오류가 발생했습니다.\n" + ex.getMessage());
		    } finally {
		        // 자원 정리
		        try { if (rs != null) rs.close(); } catch (Exception ignore) {}
		        try { if (rsKeys != null) rsKeys.close(); } catch (Exception ignore) {}
		        try { if (psOrder != null) psOrder.close(); } catch (Exception ignore) {}
		        try { if (psOrderItem != null) psOrderItem.close(); } catch (Exception ignore) {}
		        try { if (psUpdateStock != null) psUpdateStock.close(); } catch (Exception ignore) {}
		        try { if (psStampEligible != null) psStampEligible.close(); } catch (Exception ignore) {}
		        try { if (psUpdateUserReward != null) psUpdateUserReward.close(); } catch (Exception ignore) {}
		        try {
		            if (conn != null) {
		                conn.setAutoCommit(true);
		                conn.close();
		            }
		        } catch (Exception ignore) {}
		    }
		});
	}

	/** 외부에서 결제 로직 연결 */
	public void setCheckoutListener(CheckoutListener listener) {
		this.checkoutListener = listener;
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

	/** 선택 행 삭제 */
	public void removeSelected() {
		int row = table.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
			return;
		}
		model.removeRow(row);
		recalcTotal();
	}

	/** 전체 비우기 */
	public void clearAll() {
		if (model.getRowCount() == 0) return;
		int ok = JOptionPane.showConfirmDialog(this, "장바구니를 모두 비울까요?", "확인", JOptionPane.YES_NO_OPTION);
		if (ok == JOptionPane.YES_OPTION) {
			model.setRowCount(0);
			recalcTotal();
		}
	}
	
	public void clearCart() {
	    model.setRowCount(0);
	    recalcTotal();
	}

	/** 수량 변경 시 재고 한도 강제 + 금액/합계 재계산 */
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

	/** 총 금액 계산 */
	private void recalcTotal() {
		lbTotal.setText("총 금액: " + getTotal() + "원");
	}

	/** 현재 합계 반환 */
	public int getTotal() {
		int total = 0;
		for (int r = 0; r < model.getRowCount(); r++) {
			total += (Integer) model.getValueAt(r, 2);
		}
		return total;
	}

	/** 주문에 쓸 스냅샷 라인 추출 */
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

	/** 결제 콜백 인터페이스 */
	public interface CheckoutListener {
		void onCheckout(UserVO customer, List<CartLine> lines, int totalAmount);
	}

	/** 스냅샷용 라인 DTO */
	public static class CartLine {
		public final int itemId;
		public final String name;
		public final int qty;
		public final int unitPrice;  // UI에 안 보임
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