package bokchi.java.dao.jdbc;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.OrderVO;
import bokchi.java.model.enums.OrderStatus;

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

    /* =========================
       트랜잭션 조립용 단일 액션들
       (UI에서 같은 Connection으로 호출)
       ========================= */

    /** orders INSERT (user_id nullable) → 생성된 order_id 반환 */
    public int insertOrder(Connection conn, Integer userId, int totalAmount) throws SQLException {
        String sql = "INSERT INTO orders (user_id, status, total_amount) VALUES (?, 'PENDING', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (userId == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, userId);
            ps.setInt(2, totalAmount);

            if (ps.executeUpdate() != 1) throw new SQLException("주문 생성 실패");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("order_id 생성 실패");
                return rs.getInt(1);
            }
        }
    }

    /** order_items INSERT */
    public void insertOrderItem(Connection conn, int orderId, int itemId, int qty, int unitPrice) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, item_id, qty, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, itemId);
            ps.setInt(3, qty);
            ps.setInt(4, unitPrice);
            ps.executeUpdate();
        }
    }

    /** 재고 차감: stock >= qty 조건 시 차감, 아니면 false */
    public boolean decrementStockIfEnough(Connection conn, int itemId, int qty) throws SQLException {
        String sql = "UPDATE items SET stock = stock - ? WHERE item_id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, itemId);
            ps.setInt(3, qty);
            return ps.executeUpdate() == 1;
        }
    }

    /** 스탬프 적립 대상 여부 */
    public boolean isStampEligible(Connection conn, int itemId) throws SQLException {
        String sql = "SELECT stamp_eligible FROM items WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    /** 주문 상태 변경 */
    public void updateOrderStatus(Connection conn, int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    /* =========================
       기존 단독 DAO(자체 커넥션) 메서드들
       (관리 화면/목록 조회 등에 계속 사용)
       ========================= */

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
}