package bokchi.java.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.OrderVO;
import bokchi.java.model.enums.OrderStatus;

public class JdbcOrderDaoImple {
	// 싱글톤 디자인 패턴 적용
	private static JdbcOrderDaoImple instance = null;

	private JdbcOrderDaoImple() {}

	public static JdbcOrderDaoImple getInstance() {
		if (instance == null) {
			instance = new JdbcOrderDaoImple();
		}
		return instance;
	}

	// 주문 추가
	int insert(OrderVO order) {
		int result = 0;
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = ConnectionProvider.getConnection();
            String sql = "INSERT INTO orders (user_id, status, total_amount) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setNString(2, order.getStatus().name());
            ps.setInt(3, order.getTotalAmount());

            result = ps.executeUpdate();

            // 생성된 order_id 가져오기
            if (result == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    }
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

	// 주문 ID로 검색
	OrderVO findById(int orderId) {
		Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionProvider.getConnection();
            String sql = "SELECT order_id, user_id, status, total_amount, created_at FROM orders WHERE order_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnManager.close(conn, ps, rs);
        }
	}

	// 전체 주문 조회
	List<OrderVO> findAll() {
		List<OrderVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionProvider.getConnection();
            String sql = "SELECT order_id, user_id, status, total_amount, created_at FROM orders ORDER BY created_at DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            DBConnManager.close(conn, ps, rs);
        }
	}
	
	// 특정 유저 주문 조회
	List<OrderVO> findByUserId(int userId) {
		List<OrderVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionProvider.getConnection();
            String sql = "SELECT order_id, user_id, status, total_amount, created_at FROM orders WHERE user_id = ? ORDER BY created_at DESC";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            DBConnManager.close(conn, ps, rs);
        }
	}

	// 주문 상태 변경
	int updateStatus(int orderId, String status) {
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
	
	/** ResultSet → OrderVO 변환 */
	private OrderVO mapRow(ResultSet rs) throws SQLException {
	    OrderVO vo = new OrderVO();
	    vo.setOrderId(rs.getInt("order_id"));
	    vo.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
	    vo.setStatus(OrderStatus.valueOf(rs.getString("status")));
	    vo.setTotalAmount(rs.getInt("total_amount"));
	    java.sql.Timestamp ts = rs.getTimestamp("created_at");
	    if (ts != null) {
	        vo.setOrderTime(ts.toLocalDateTime());
	    }

	    return vo;
	}
}
