package bokchi.java.dao.jdbc;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.OrderVO;
import bokchi.java.model.UserVO;
import bokchi.java.model.enums.OrderStatus;
import bokchi.java.ui.user.CartPanel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderDaoImple {
	public static final int FREE_DRINK_COST = 12;

	// 싱글톤 디자인 패턴 적용
		private static JdbcOrderDaoImple instance = null;

		private JdbcOrderDaoImple() {}

		public static JdbcOrderDaoImple getInstance() {
			if (instance == null) {
				instance = new JdbcOrderDaoImple();
			}
			return instance;
		}

	public int insert(OrderVO order) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "INSERT INTO orders (user_id, status, total_amount) VALUES (?, ?, ?)";
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			if (order.getUserId() == null) ps.setNull(1, Types.INTEGER);
			else ps.setInt(1, order.getUserId());

			ps.setString(2, order.getStatus().name());
			ps.setInt(3, order.getTotalAmount());

			int result = ps.executeUpdate();

			if (result == 1) {
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) order.setOrderId(rs.getInt(1));
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			DBConnManager.close(conn, ps);
		}
	}

	// 주문 아이디로 검색
	public OrderVO findById(int orderId) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT order_id, user_id, status, total_amount, order_time FROM orders WHERE order_id = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, orderId);
			rs = ps.executeQuery();
			return rs.next() ? mapRow(rs) : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
	}

	// 최신순 전체 주문 조회
	public List<OrderVO> findAll() {
		List<OrderVO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT order_id, user_id, status, total_amount, order_time FROM orders ORDER BY order_time DESC";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) list.add(mapRow(rs));
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return list;
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
	}

	// 최신순 유저 주문 조회
	public List<OrderVO> findByUserId(int userId) {
		List<OrderVO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT order_id, user_id, status, total_amount, order_time FROM orders WHERE user_id = ? ORDER BY order_time DESC";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) list.add(mapRow(rs));
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return list;
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
	}

	// 주문 상태 변경
	public int updateStatus(int orderId, String status) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, status);
			ps.setInt(2, orderId);
			return ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			DBConnManager.close(conn, ps);
		}
	}

	private OrderVO mapRow(ResultSet rs) throws SQLException {
		OrderVO vo = new OrderVO();
		vo.setOrderId(rs.getInt("order_id"));
		Object uid = rs.getObject("user_id");
		vo.setUserId(uid == null ? null : ((Number) uid).intValue());
		vo.setStatus(OrderStatus.valueOf(rs.getString("status")));
		vo.setTotalAmount(rs.getInt("total_amount"));
		Timestamp ts = rs.getTimestamp("order_time");
		if (ts != null) vo.setOrderTime(ts.toLocalDateTime());
		return vo;
	}
	
	public CheckoutResult checkout(UserVO customer,
			List<CartPanel.CartLine> lines,
			int uiTotalAmount,
			boolean usedFreeDrink,
			Integer freeDrinkItemId) throws SQLException {
		try (Connection conn = ConnectionProvider.getConnection()) {
			conn.setAutoCommit(false);
			try {
				// 1) 서버 재계산
				int total = 0;
				for (var l : lines) total += l.lineAmount;

				// 2) orders INSERT
				int orderId;
				String sqlOrder = "INSERT INTO orders (user_id, status, total_amount) VALUES (?, 'PENDING', ?)";
				try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
					if (customer == null || customer.getUserId() == 0) ps.setNull(1, Types.INTEGER);
					else ps.setInt(1, customer.getUserId());
					ps.setInt(2, total);
					if (ps.executeUpdate() != 1) throw new SQLException("주문 생성 실패");
					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (!rs.next()) throw new SQLException("order_id 생성 실패");
						orderId = rs.getInt(1);
					}
				}

				// 3) 라인 저장 + 재고 차감 + 적립 계산
				String sqlItem  = "INSERT INTO order_items (order_id, item_id, qty, unit_price) VALUES (?, ?, ?, ?)";
				String sqlStock = "UPDATE items SET stock = stock - ? WHERE item_id = ? AND stock >= ?";
				String sqlElig  = "SELECT stamp_eligible FROM items WHERE item_id = ?";

				int stampsEarned = 0;

				try (PreparedStatement psItem = conn.prepareStatement(sqlItem);
						PreparedStatement psStock = conn.prepareStatement(sqlStock);
						PreparedStatement psElig  = conn.prepareStatement(sqlElig)) {

					for (var l : lines) {
						// order_items
						psItem.clearParameters();
						psItem.setInt(1, orderId);
						psItem.setInt(2, l.itemId);
						psItem.setInt(3, l.qty);
						psItem.setInt(4, l.unitPrice); // 무료 라인은 0
						psItem.executeUpdate();

						// 재고 차감
						psStock.clearParameters();
						psStock.setInt(1, l.qty);
						psStock.setInt(2, l.itemId);
						psStock.setInt(3, l.qty);
						if (psStock.executeUpdate() != 1) {
							throw new SQLException("재고 부족/동시성 충돌: item_id=" + l.itemId);
						}

						// 적립 가능 + 무료 제외
						psElig.clearParameters();
						psElig.setInt(1, l.itemId);
						try (ResultSet rs = psElig.executeQuery()) {
							boolean eligible = false;
							if (rs.next()) eligible = rs.getBoolean(1);
							if (eligible && l.unitPrice > 0) {
								stampsEarned += l.qty; // 1잔당 1개
							}
						}
					}
				}

				// 4) 무료 음료 사용 시 스탬프 차감 + 기록
				if (usedFreeDrink && customer != null && customer.getUserId() != 0) {
					try (PreparedStatement ps = conn.prepareStatement(
							"UPDATE users SET reward_balance = reward_balance - ? " +
							"WHERE user_id = ? AND reward_balance >= ?")) {
						ps.setInt(1, FREE_DRINK_COST);
						ps.setInt(2, customer.getUserId());
						ps.setInt(3, FREE_DRINK_COST);
						if (ps.executeUpdate() != 1) throw new SQLException("스탬프 부족(무료 사용 실패)");
					}
					try (PreparedStatement ps = conn.prepareStatement(
							"INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'REDEEM')")) {
						ps.setInt(1, customer.getUserId());
						ps.setInt(2, orderId);
						ps.setInt(3, -FREE_DRINK_COST);
						ps.executeUpdate();
					}
				}

				// 5) 적립 + 기록
				if (customer != null && customer.getUserId() != 0 && stampsEarned > 0) {
					try (PreparedStatement ps = conn.prepareStatement(
							"UPDATE users SET reward_balance = reward_balance + ? WHERE user_id = ?")) {
						ps.setInt(1, stampsEarned);
						ps.setInt(2, customer.getUserId());
						ps.executeUpdate();
					}
					try (PreparedStatement ps = conn.prepareStatement(
							"INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'EARN')")) {
						ps.setInt(1, customer.getUserId());
						ps.setInt(2, orderId);
						ps.setInt(3, stampsEarned);
						ps.executeUpdate();
					}
				}

				// 6) PAID
				try (PreparedStatement ps = conn.prepareStatement(
						"UPDATE orders SET status='PAID' WHERE order_id = ?")) {
					ps.setInt(1, orderId);
					ps.executeUpdate();
				}

				conn.commit();
				return new CheckoutResult(orderId, total, stampsEarned, usedFreeDrink);

			} catch (SQLException ex) {
				conn.rollback();
				throw ex;
			} finally {
				conn.setAutoCommit(true);
			}
		}
	}

	// 결제 결과
	public static class CheckoutResult {
		private final int orderId;
		private final int totalAmount;
		private final int stampsEarned;
		private final boolean usedFreeDrink;

		public CheckoutResult(int orderId, int totalAmount, int stampsEarned, boolean usedFreeDrink) {
			this.orderId = orderId;
			this.totalAmount = totalAmount;
			this.stampsEarned = stampsEarned;
			this.usedFreeDrink = usedFreeDrink;
		}

		public int getOrderId() { return orderId; }
		public int getTotalAmount() { return totalAmount; }
		public int getStampsEarned() { return stampsEarned; }
		public boolean isUsedFreeDrink() { return usedFreeDrink; }

		// 축약
		public int orderId() { return orderId; }
		public int totalAmount() { return totalAmount; }
		public int stampsEarned() { return stampsEarned; }
		public boolean usedFreeDrink() { return usedFreeDrink; }
	}
}