package bokchi.java.dao.jdbc;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.RewardHistoryVO;
import bokchi.java.model.enums.RewardReason;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcRewardHistoryDaoImple {
	// 싱글톤 디자인 패턴 적용
	private static JdbcRewardHistoryDaoImple instance = null;

	private JdbcRewardHistoryDaoImple() {}

	public static JdbcRewardHistoryDaoImple getInstance() {
		if (instance == null) {
			instance = new JdbcRewardHistoryDaoImple();
		}
		return instance;
	}

	//공통 매핑
	private RewardHistoryVO mapRow(ResultSet rs) throws SQLException {
		RewardHistoryVO vo = new RewardHistoryVO();
		vo.setHistoryId(rs.getInt("history_id"));
		vo.setUserId(rs.getInt("user_id"));
		int oid = rs.getInt("order_id");
		vo.setOrderId(rs.wasNull() ? null : oid);
		vo.setDelta(rs.getInt("delta"));
		vo.setReason(RewardReason.valueOf(rs.getString("reason")));
		Timestamp ts = rs.getTimestamp("created_at");
		vo.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
		return vo;
	}

	// 삽입
	public int insert(RewardHistoryVO vo) {
		int result = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet keys = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, ?)";
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, vo.getUserId());
			if (vo.getOrderId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, vo.getOrderId());
			ps.setInt(3, vo.getDelta());
			ps.setString(4, vo.getReason().name());
			result = ps.executeUpdate();
			if (result == 1) {
				keys = ps.getGeneratedKeys();
				if (keys.next()) vo.setHistoryId(keys.getInt(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (keys != null) keys.close(); } catch (Exception ignore) {}
			DBConnManager.close(conn, ps);
		}
		return result;
	}

	// 아이디로 조회
	public RewardHistoryVO findById(int historyId) {
		RewardHistoryVO vo = null;
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			ps = conn.prepareStatement("SELECT * FROM reward_history WHERE history_id = ?");
			ps.setInt(1, historyId);
			rs = ps.executeQuery();
			if (rs.next()) vo = mapRow(rs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		return vo;
	}

	// 유저아이디로 조회
	public List<RewardHistoryVO> findByUserId(int userId) {
		List<RewardHistoryVO> list = new ArrayList<>();
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			ps = conn.prepareStatement("SELECT * FROM reward_history WHERE user_id = ? ORDER BY history_id DESC");
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) list.add(mapRow(rs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		return list;
	}

	// 오더아이디로 조회
	public List<RewardHistoryVO> findByOrderId(int orderId) {
		List<RewardHistoryVO> list = new ArrayList<>();
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			ps = conn.prepareStatement("SELECT * FROM reward_history WHERE order_id = ? ORDER BY history_id DESC");
			ps.setInt(1, orderId);
			rs = ps.executeQuery();
			while (rs.next()) list.add(mapRow(rs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		return list;
	}

	// 전체 조회
	public List<RewardHistoryVO> findAll() {
		List<RewardHistoryVO> list = new ArrayList<>();
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			ps = conn.prepareStatement("SELECT * FROM reward_history ORDER BY history_id DESC");
			rs = ps.executeQuery();
			while (rs.next()) list.add(mapRow(rs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		return list;
	}

	// 기간 조회
	public List<RewardHistoryVO> findByUserIdBetween(int userId, LocalDateTime from, LocalDateTime to) {
		List<RewardHistoryVO> list = new ArrayList<>();
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM reward_history WHERE user_id = ? AND created_at BETWEEN ? AND ? ORDER BY history_id DESC";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.setTimestamp(2, Timestamp.valueOf(from));
			ps.setTimestamp(3, Timestamp.valueOf(to));
			rs = ps.executeQuery();
			while (rs.next()) list.add(mapRow(rs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		return list;
	}

	// 최근 조회
	public List<RewardHistoryVO> findRecentByUserId(int userId, int limit) {
		List<RewardHistoryVO> list = new ArrayList<>();
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM reward_history WHERE user_id = ? ORDER BY history_id DESC LIMIT ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.setInt(2, limit);
			rs = ps.executeQuery();
			while (rs.next()) list.add(mapRow(rs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		return list;
	}

	// 편의 메서드
	public int recordEarn(int userId, Integer orderId, int stamps) {
		RewardHistoryVO vo = new RewardHistoryVO();
		vo.setUserId(userId);
		vo.setOrderId(orderId);
		vo.setDelta(stamps);
		vo.setReason(RewardReason.EARN);
		return insert(vo);
	}

	public int recordRedeem(int userId, Integer orderId, int stampsUsed) {
		RewardHistoryVO vo = new RewardHistoryVO();
		vo.setUserId(userId);
		vo.setOrderId(orderId);
		vo.setDelta(-Math.abs(stampsUsed));
		vo.setReason(RewardReason.REDEEM);
		return insert(vo);
	}

	public int recordAdjust(int userId, int delta, String noteIgnored) {
		RewardHistoryVO vo = new RewardHistoryVO();
		vo.setUserId(userId);
		vo.setOrderId(null);
		vo.setDelta(delta);
		vo.setReason(RewardReason.ADJUST);
		return insert(vo);
	}

	// Connection을 외부에서 넘김
	public int insert(Connection conn, RewardHistoryVO vo) throws SQLException {
		String sql = "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, vo.getUserId());
			if (vo.getOrderId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, vo.getOrderId());
			ps.setInt(3, vo.getDelta());
			ps.setString(4, vo.getReason().name());
			int result = ps.executeUpdate();
			if (result == 1) {
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) vo.setHistoryId(rs.getInt(1));
				}
			}
			return result;
		}
	}

	public int recordEarn(Connection conn, int userId, Integer orderId, int stamps) throws SQLException {
		String sql = "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'EARN')";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			if (orderId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, orderId);
			ps.setInt(3, stamps);
			return ps.executeUpdate();
		}
	}

	public int recordRedeem(Connection conn, int userId, Integer orderId, int stampsUsed) throws SQLException {
		String sql = "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'REDEEM')";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			if (orderId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, orderId);
			ps.setInt(3, -Math.abs(stampsUsed));
			return ps.executeUpdate();
		}
	}

	public int recordAdjust(Connection conn, int userId, int delta, String noteIgnored) throws SQLException {
		String sql = "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, NULL, ?, 'ADJUST')";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ps.setInt(2, delta);
			return ps.executeUpdate();
		}
	}
}