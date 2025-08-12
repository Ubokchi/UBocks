package bokchi.java.ui.user;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.model.ItemVO;
import bokchi.java.model.UserVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
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
	private CheckoutListener checkoutListener; // 결제 콜백(미사용시 null)

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

		// 합계]
		lbTotal.setFont(lbTotal.getFont().deriveFont(Font.BOLD, 14f));
		JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		totalPanel.add(lbTotal);

		// 버튼 3개
		JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		btns.add(btnRemove);
		btns.add(btnClear);
		btns.add(btnCheckout);

		// 두 줄 배치
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
		bottom.add(totalPanel);
		bottom.add(btns);

		add(bottom, BorderLayout.SOUTH);

		// 수량 변경 시 재고 금액 합계 반영
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

	// 무료 아이템 추가 - 일단 안 쓰고 있음
	public void addFreeItem(ItemVO vo) {
		if (!vo.isActive()) { JOptionPane.showMessageDialog(this, "판매 중이 아닌 상품입니다."); return; }
		if (vo.getStock() <= 0) { JOptionPane.showMessageDialog(this, "재고가 없습니다."); return; }
		model.addRow(new Object[]{
				vo.getName() + " (무료)", // 0
				1,                        // 1
				0,                        // 2
				vo.getItemId(),           // 3
				0,                        // 4
				vo.getStock()             // 5
		});
		recalcTotal();
	}

	//선택 행 삭제
	public void removeSelected() {
		int row = table.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
			return;
		}
		model.removeRow(row);
		recalcTotal();
	}

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

	// 재고 한도 강제 /수량 재계산
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

	// 현재 합계 리턴
	public int getTotal() {
		int total = 0;
		for (int r = 0; r < model.getRowCount(); r++) {
			total += (Integer) model.getValueAt(r, 2);
		}
		return total;
	}

	//주문에 쓸 스냅샷 라인
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

	public interface CheckoutListener {
		void onCheckout(UserVO customer, List<CartLine> lines, int totalAmount);
	}
	
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

	private void onCheckout() {
		// 0) 장바구니 비었는지 확인
		if (model.getRowCount() == 0) {
			JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
			return;
		}

		// 임계치 이상이면 장바구니 대상 선택 → 1잔 무료 적용
		final int COST = bokchi.java.service.RewardService.FREE_DRINK_COST;
		boolean usedFreeDrink = false;
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
				Integer targetItemId = dlg.getSelectedItemId();
				if (targetItemId == null) {
					JOptionPane.showMessageDialog(this, "무료 적용 대상을 선택하지 않았습니다.");
				} else {
					boolean ok = applyFreeToCart(targetItemId);
					if (!ok) {
						JOptionPane.showMessageDialog(this, "선택한 상품을 장바구니에서 찾지 못했거나 적용할 수 없습니다.");
					} else {
						usedFreeDrink = true;
					}
				}
			}
		}

		int confirm = JOptionPane.showConfirmDialog(
				this, "결제를 진행하시겠습니까?", "결제 확인", JOptionPane.YES_NO_OPTION
				);
		if (confirm != JOptionPane.YES_OPTION) return;

		if (checkoutListener != null) {
			checkoutListener.onCheckout(customer, snapshotLines(), getTotal());
			clearCart();
			return;
		}

		Connection conn = null;
		PreparedStatement psOrder = null;
		PreparedStatement psOrderItem = null;
		PreparedStatement psUpdateStock = null;
		PreparedStatement psStampEligible = null;
		PreparedStatement psUpdateUserReward = null;
		PreparedStatement psRedeem = null;
		ResultSet rsKeys = null;
		ResultSet rs = null;

		try {
			// 1) 시작
			conn = ConnectionProvider.getConnection();
			conn.setAutoCommit(false);

			// 2) 합계 계산
			int total = getTotal();

			// 3) orders INSER
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
				int unit   = (Integer) model.getValueAt(r, 4); // 무료 라인은 0

				// 5-1) order_items INSERT
				psOrderItem.clearParameters();
				psOrderItem.setInt(1, orderId);
				psOrderItem.setInt(2, itemId);
				psOrderItem.setInt(3, qty);
				psOrderItem.setInt(4, unit);
				psOrderItem.executeUpdate();

				// 5-2) 재고 차감
				psUpdateStock.clearParameters();
				psUpdateStock.setInt(1, qty);
				psUpdateStock.setInt(2, itemId);
				psUpdateStock.setInt(3, qty);
				int updated = psUpdateStock.executeUpdate();
				if (updated != 1) throw new SQLException("재고 부족 또는 동시성 충돌: item_id=" + itemId);

				// 5-3) 스탬프 적립 가능 여부
				psStampEligible.clearParameters();
				psStampEligible.setInt(1, itemId);
				rs = psStampEligible.executeQuery();
				boolean eligible = false;
				if (rs.next()) eligible = rs.getBoolean(1);
				rs.close(); rs = null;

				// 무료(unit=0) 라인 제외, eligible + unit>0 인 라인만 적립
				if (eligible && unit > 0) stampsEarned += qty;
			}

			// 6-a) 무료 음료 사용 시 스탬프 차감
			if (usedFreeDrink && customer != null && customer.getUserId() != 0) {
				String sqlRedeem = "UPDATE users SET reward_balance = reward_balance - ? WHERE user_id = ? AND reward_balance >= ?";
				psRedeem = conn.prepareStatement(sqlRedeem);
				psRedeem.setInt(1, bokchi.java.service.RewardService.FREE_DRINK_COST);
				psRedeem.setInt(2, customer.getUserId());
				psRedeem.setInt(3, bokchi.java.service.RewardService.FREE_DRINK_COST);
				int ok = psRedeem.executeUpdate();
				if (ok != 1) throw new SQLException("스탬프가 부족합니다(무료 음료 차감 실패).");

				// reward_history(REDEEM) 기록
				try (PreparedStatement ps = conn.prepareStatement(
						"INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'REDEEM')")) {
					ps.setInt(1, customer.getUserId());
					ps.setInt(2, orderId);
					ps.setInt(3, -bokchi.java.service.RewardService.FREE_DRINK_COST);
					ps.executeUpdate();
				}
			}

			// 6-b) 스탬프 적립 반영
			if (customer != null && customer.getUserId() != 0 && stampsEarned > 0) {
				String sqlReward = "UPDATE users SET reward_balance = reward_balance + ? WHERE user_id = ?";
				psUpdateUserReward = conn.prepareStatement(sqlReward);
				psUpdateUserReward.setInt(1, stampsEarned);
				psUpdateUserReward.setInt(2, customer.getUserId());
				psUpdateUserReward.executeUpdate();

				// reward_history(EARN) 기록
				try (PreparedStatement ps = conn.prepareStatement(
						"INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'EARN')")) {
					ps.setInt(1, customer.getUserId());
					ps.setInt(2, orderId);
					ps.setInt(3, stampsEarned);
					ps.executeUpdate();
				}
			}

			// 7) PAID로 변경
			try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status='PAID' WHERE order_id = ?")) {
				ps.setInt(1, orderId);
				ps.executeUpdate();
			}
			
			conn.commit();

			// 9) 성공 안내 + 장바구니 비우기
			JOptionPane.showMessageDialog(this,
					"결제가 완료되었습니다.\n주문번호: " + orderId +
					"\n합계: " + total + "원\n적립 스탬프: " + stampsEarned + "개" +
					(usedFreeDrink ? "\n무료 음료 1잔 사용" : ""));
			model.setRowCount(0);
			lbTotal.setText("총 금액: 0원");

		} catch (Exception ex) {
			try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "결제 처리 중 오류가 발생했습니다.\n" + ex.getMessage());
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception ignore) {}
			try { if (rsKeys != null) rsKeys.close(); } catch (Exception ignore) {}
			try { if (psOrder != null) psOrder.close(); } catch (Exception ignore) {}
			try { if (psOrderItem != null) psOrderItem.close(); } catch (Exception ignore) {}
			try { if (psUpdateStock != null) psUpdateStock.close(); } catch (Exception ignore) {}
			try { if (psStampEligible != null) psStampEligible.close(); } catch (Exception ignore) {}
			try { if (psUpdateUserReward != null) psUpdateUserReward.close(); } catch (Exception ignore) {}
			try { if (psRedeem != null) psRedeem.close(); } catch (Exception ignore) {}
			try {
				if (conn != null) {
					conn.setAutoCommit(true);
					conn.close();
				}
			} catch (Exception ignore) {}
		}
	}

	// 장바구니에서 itemId 대상 라인에 '1잔 무료' 적용: 원 라인 qty-1, 0원 라인 +1 추가
	private boolean applyFreeToCart(int itemId) {
		for (int r = 0; r < model.getRowCount(); r++) {
			int rid = (Integer) model.getValueAt(r, 3);   // itemId
			int qty = (Integer) model.getValueAt(r, 1);
			int unit= (Integer) model.getValueAt(r, 4);

			if (rid == itemId && unit > 0 && qty > 0) {
				// 원 라인 수량 1 감소
				int newQty = qty - 1;
				model.setValueAt(newQty, r, 1);
				model.setValueAt(newQty * unit, r, 2);

				// 0원 라인 추가
				String name = (String) model.getValueAt(r, 0);
				int stock   = (Integer) model.getValueAt(r, 5);
				model.addRow(new Object[]{
						name + " (무료)", // 0
						1,                // 1
						0,                // 2
						itemId,           // 3
						0,                // 4
						stock             // 5
				});

				// 원 라인이 0개가 되면 삭제
				if (newQty == 0) model.removeRow(r);

				recalcTotal();
				return true;
			}
		}
		return false;
	}
}