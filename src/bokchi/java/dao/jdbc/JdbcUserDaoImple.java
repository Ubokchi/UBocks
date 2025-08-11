package bokchi.java.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.UserVO;

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

	// 회원 추가
	public int insert(UserVO user) {
		int result = 0;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ConnectionProvider.getConnection();
			String sql = "INSERT INTO users "
					+ "(username, password, role, phone, name, reward_balance) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPassword());
			ps.setString(3, user.getRole().name());  // enum 을 String
			ps.setString(4, user.getPhone());
			ps.setString(5, user.getName());
			ps.setInt(6, user.getRewardBalance());

			result = ps.executeUpdate(); 
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			DBConnManager.close(conn, ps);
		}
	}

	// username으로 검색
	public UserVO findByUsername(String username) {
		UserVO vo = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM users "
					+ "WHERE username = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);

			rs = ps.executeQuery();
			if (rs.next()) {
				vo = new UserVO();
				vo.setUserId(rs.getInt("user_id"));
				vo.setUsername(rs.getString("username"));
				vo.setPassword(rs.getString("password"));
				vo.setRole(bokchi.java.model.enums.Role.valueOf(rs.getString("role")));
				vo.setPhone(rs.getString("phone"));
				vo.setName(rs.getString("name"));
				vo.setRewardBalance(rs.getInt("reward_balance"));
				java.sql.Timestamp ts = rs.getTimestamp("created_at");
				vo.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}

		return vo;	// 없으면 null retrun함
	}

	// ID로 검색
	public UserVO findById(int userId) {                   
		UserVO vo = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM users "
					+ "WHERE user_id = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);

			rs = ps.executeQuery();
			if (rs.next()) {
				vo = new UserVO();
				vo.setUserId(rs.getInt("user_id"));
				vo.setUsername(rs.getString("username"));
				vo.setPassword(rs.getString("password"));
				vo.setRole(bokchi.java.model.enums.Role.valueOf(rs.getString("role")));
				vo.setPhone(rs.getString("phone"));
				vo.setName(rs.getString("name"));
				vo.setRewardBalance(rs.getInt("reward_balance"));
				java.sql.Timestamp ts = rs.getTimestamp("created_at");
				vo.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}

		return vo;	// 없으면 null retrun함
	}

	// 전체 조회
	public List<UserVO> findAll() {
		ArrayList<UserVO> list = new ArrayList<UserVO>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM users ORDER BY user_id DESC";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				UserVO vo = new UserVO();
				vo.setUserId(rs.getInt("user_id"));
				vo.setUsername(rs.getString("username"));
				vo.setPassword(rs.getString("password"));
				vo.setRole(bokchi.java.model.enums.Role.valueOf(rs.getString("role")));
				vo.setPhone(rs.getString("phone"));
				vo.setName(rs.getString("name"));
				vo.setRewardBalance(rs.getInt("reward_balance"));
				java.sql.Timestamp ts = rs.getTimestamp("created_at");
				vo.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

				list.add(vo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}

		return list;
	}

	// 회원 정보 수정
	public int update(int userId, UserVO user) {
	    int result = 0;

	    Connection conn = null;
	    PreparedStatement ps = null;

	    try {
	        conn = ConnectionProvider.getConnection();
	        String sql = "UPDATE users "
	                   + "SET username = ?,"
	                   + "password = ?, "
	                   + "role = ?, "
	                   + "phone = ?, "
	                   + "name = ?, "
	                   + "reward_balance = ? "
	                   + "WHERE user_id = ?";

	        ps = conn.prepareStatement(sql);
	        ps.setString(1, user.getUsername());
	        ps.setString(2, user.getPassword());
	        ps.setString(3, user.getRole().name());
	        ps.setString(4, user.getPhone());
	        ps.setString(5, user.getName());
	        ps.setInt(6, user.getRewardBalance());
	        ps.setInt(7, userId);

	        result = ps.executeUpdate();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        DBConnManager.close(conn, ps);
	    }

	    return result;
	}

	// 회원 삭제
	public int delete(int userId) {
		int result = 0;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ConnectionProvider.getConnection();
			String sql = "delete from users where user_id = ?";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);

            result = ps.executeUpdate();


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps);
		}

		return result;
	}
}