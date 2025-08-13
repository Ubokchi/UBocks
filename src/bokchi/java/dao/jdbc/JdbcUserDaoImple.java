package bokchi.java.dao.jdbc;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.UserVO;
import bokchi.java.model.enums.Role;

import java.sql.*;

/**
 * users 테이블 전용 DAO (싱글톤)
 * - UI/서비스 트랜잭션에서 사용할 수 있도록 Connection 인자를 받는 메서드도 제공
 */
public class JdbcUserDaoImple {

	// 싱글톤 디자인 패턴 적용
		private static JdbcUserDaoImple instance = null;

		private JdbcUserDaoImple() {}

		public static JdbcUserDaoImple getInstance() {
			if (instance == null) {
				instance = new JdbcUserDaoImple();
			}
			return instance;
		}

    // -----------------
    // Mapping
    // -----------------
    private UserVO mapRow(ResultSet rs) throws SQLException {
        UserVO vo = new UserVO();
        vo.setUserId(rs.getInt("user_id"));
        vo.setUsername(rs.getString("username"));
        vo.setPassword(rs.getString("password"));

        String roleStr = rs.getString("role");
        vo.setRole(roleStr != null ? Role.valueOf(roleStr) : null);

        vo.setName(rs.getString("name"));
        vo.setPhone(rs.getString("phone"));
        vo.setRewardBalance(rs.getInt("reward_balance")); // NOT NULL 가정(기본 0)

        Timestamp ts = null;
        try { ts = rs.getTimestamp("created_at"); } catch (SQLException ignore) {}
        if (ts != null) vo.setCreatedAt(ts.toLocalDateTime());

        return vo;
    }

    // -----------------
    // CRUD (Connection 자체를 내부에서 열고 닫는 버전)
    // -----------------

    /** username 중복 체크/로그인용 */
    public UserVO findByUsername(String username) {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = ConnectionProvider.getConnection();
            ps = conn.prepareStatement(
                "SELECT user_id, username, password, role, name, phone, reward_balance, created_at " +
                "FROM users WHERE username = ?"
            );
            ps.setString(1, username);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnManager.close(conn, ps, rs);
        }
    }

    /** PK 조회 */
    public UserVO findById(int userId) {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = ConnectionProvider.getConnection();
            ps = conn.prepareStatement(
                "SELECT user_id, username, password, role, name, phone, reward_balance, created_at " +
                "FROM users WHERE user_id = ?"
            );
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnManager.close(conn, ps, rs);
        }
    }

    /** INSERT (username, phone은 UNIQUE 가정) */
    public int insert(UserVO vo) throws SQLException {
        Connection conn = null; PreparedStatement ps = null; ResultSet keys = null;
        try {
            conn = ConnectionProvider.getConnection();
            ps = conn.prepareStatement(
                "INSERT INTO users (username, password, role, name, phone, reward_balance) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, vo.getUsername());
            ps.setString(2, vo.getPassword());
            ps.setString(3, vo.getRole() != null ? vo.getRole().name() : Role.CUSTOMER.name());
            ps.setString(4, vo.getName());
            ps.setString(5, vo.getPhone());
            ps.setInt(6, vo.getRewardBalance());

            int r = ps.executeUpdate();
            if (r == 1) {
                keys = ps.getGeneratedKeys();
                if (keys.next()) vo.setUserId(keys.getInt(1));
            }
            return r;
        } finally {
            try { if (keys != null) keys.close(); } catch (Exception ignore) {}
            DBConnManager.close(conn, ps);
        }
    }

    /** UPDATE(비번/연락처/이름 등 일부 업데이트 예시) */
    public int updateBasic(UserVO vo) throws SQLException {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = ConnectionProvider.getConnection();
            ps = conn.prepareStatement(
                "UPDATE users SET password=?, name=?, phone=? WHERE user_id=?"
            );
            ps.setString(1, vo.getPassword());
            ps.setString(2, vo.getName());
            ps.setString(3, vo.getPhone());
            ps.setInt(4, vo.getUserId());
            return ps.executeUpdate();
        } finally {
            DBConnManager.close(conn, ps);
        }
    }

    public int delete(int userId) throws SQLException {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = ConnectionProvider.getConnection();
            ps = conn.prepareStatement("DELETE FROM users WHERE user_id=?");
            ps.setInt(1, userId);
            return ps.executeUpdate();
        } finally {
            DBConnManager.close(conn, ps);
        }
    }

    // -----------------
    // 리워드(스탬프) 관련 — 트랜잭션에서 사용할 수 있게 Connection 받는 버전 우선
    // -----------------

    /**
     * 스탬프 증감 (가드 포함)
     * - delta가 음수일 수 있으며, 결과가 음수가 되면 실패(0 rows)
     * - 실패 시 SQLException 던짐 (상위에서 rollback)
     */
    public void addToRewardBalanceGuarded(Connection conn, int userId, int delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET reward_balance = reward_balance + ? " +
                "WHERE user_id = ? AND reward_balance + ? >= 0")) {
            ps.setInt(1, delta);
            ps.setInt(2, userId);
            ps.setInt(3, delta);
            int r = ps.executeUpdate();
            if (r != 1) throw new SQLException("스탬프 부족 또는 사용자 없음");
        }
    }

    /** 스탬프 증감 (가드 없음: 결과가 음수여도 DB가 허용하면 적용) */
    public void addToRewardBalance(Connection conn, int userId, int delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET reward_balance = reward_balance + ? WHERE user_id = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** 현재 스탬프 조회 (트랜잭션 내 사용 가능) */
    public int getRewardBalance(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT reward_balance FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("사용자 없음: user_id=" + userId);
            }
        }
    }

    // -----------------
    // (선택) Auto-connection 헬퍼 버전 — 트랜잭션 외부에서 간단 호출용
    // -----------------

    public void addToRewardBalanceGuarded(int userId, int delta) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);
            addToRewardBalanceGuarded(conn, userId, delta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ignore) {}
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (Exception ignore) {}
            DBConnManager.close(conn, null);
        }
    }

    public void addToRewardBalance(int userId, int delta) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);
            addToRewardBalance(conn, userId, delta);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ignore) {}
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (Exception ignore) {}
            DBConnManager.close(conn, null);
        }
    }
    
    public UserVO findByPhone(String phone) {
        Connection conn=null; PreparedStatement ps=null; ResultSet rs=null;
        try {
            conn = ConnectionProvider.getConnection();
            ps = conn.prepareStatement("SELECT * FROM users WHERE phone = ?");
            ps.setString(1, phone);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs); // 기존 mapRow 재사용
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DBConnManager.close(conn, ps, rs);
        }
    }
}