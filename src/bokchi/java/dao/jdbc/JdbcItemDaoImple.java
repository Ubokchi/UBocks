package bokchi.java.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.ItemVO;
import bokchi.java.model.enums.ItemType;

public class JdbcItemDaoImple {
	// 싱글톤 디자인 패턴 적용
	private static JdbcItemDaoImple instance = null;

	private JdbcItemDaoImple() {}

	public static JdbcItemDaoImple getInstance() {
		if (instance == null) {
			instance = new JdbcItemDaoImple();
		}
		return instance;
	}

	// 상품 추가
	public int insert(ItemVO item) {
		int result = 0;
		
		Connection conn = null; 
		PreparedStatement ps = null;
		
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "INSERT INTO items (type, name, price, stock, is_active, stamp_eligible) " +
					"VALUES (?, ?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getType().name());
			ps.setString(2, item.getName());
			ps.setInt(3, item.getPrice());
			ps.setInt(4, item.getStock());
			ps.setBoolean(5, item.isActive());
			ps.setBoolean(6, item.isStampEligible());
			
			result = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps);
		}
		
		return result;
	}

	// ID로 검색
	public ItemVO findById(int itemId) {
		ItemVO vo = null;
		
		Connection conn = null; 
		PreparedStatement ps = null; 
		ResultSet rs = null;
		
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM items WHERE item_id = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, itemId);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				vo = new ItemVO();
		        vo.setItemId(rs.getInt("item_id"));
		        vo.setType(ItemType.valueOf(rs.getString("type")));
		        vo.setName(rs.getString("name"));
		        vo.setPrice(rs.getInt("price"));
		        vo.setStock(rs.getInt("stock"));
		        vo.setActive(rs.getBoolean("is_active"));
		        vo.setStampEligible(rs.getBoolean("stamp_eligible"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		
		return vo;
	}

	// 전체 조회
	public List<ItemVO> findAll() {
		ItemVO vo = null;
		
		List<ItemVO> list = new ArrayList<>();
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM items ORDER BY item_id DESC";
			
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				vo = new ItemVO();
		        vo.setItemId(rs.getInt("item_id"));
		        vo.setType(ItemType.valueOf(rs.getString("type")));
		        vo.setName(rs.getString("name"));
		        vo.setPrice(rs.getInt("price"));
		        vo.setStock(rs.getInt("stock"));
		        vo.setActive(rs.getBoolean("is_active"));
		        vo.setStampEligible(rs.getBoolean("stamp_eligible"));
		        
		        list.add(vo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		
		return list;
	}

	// 상품 수정
	public int update(ItemVO item) {
		int result = 0;
		
		Connection conn = null; PreparedStatement ps = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "UPDATE items SET type=?, name=?, price=?, stock=?, is_active=?, stamp_eligible=? " +
					"WHERE item_id = ?";
			
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getType().name());
			ps.setString(2, item.getName());
			ps.setInt(3, item.getPrice());
			ps.setInt(4, item.getStock());
			ps.setBoolean(5, item.isActive());
			ps.setBoolean(6, item.isStampEligible());
			ps.setInt(7, item.getItemId());
			result = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps);
		}
		return result;
	}

	// 상품 삭제
	public int delete(int itemId) {
		int result = 0;
		
		Connection conn = null; PreparedStatement ps = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "DELETE FROM items WHERE item_id = ?";
			
			ps = conn.prepareStatement(sql);
			ps.setInt(1, itemId);
			
			result = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps);
		}
		return result;
	}

	// 타입별 활성 상품 조회 - 판매 중
	public List<ItemVO> findActiveByType(ItemType type) {
		ItemVO vo = null;
		
		List<ItemVO> list = new ArrayList<>();
		Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
		try {
			conn = ConnectionProvider.getConnection();
			String sql = "SELECT * FROM items WHERE is_active = TRUE AND type = ? ORDER BY name";
			
			ps = conn.prepareStatement(sql);
			ps.setString(1, type.name());
			
			rs = ps.executeQuery();
			while (rs.next()) {
				vo = new ItemVO();
		        vo.setItemId(rs.getInt("item_id"));
		        vo.setType(ItemType.valueOf(rs.getString("type")));
		        vo.setName(rs.getString("name"));
		        vo.setPrice(rs.getInt("price"));
		        vo.setStock(rs.getInt("stock"));
		        vo.setActive(rs.getBoolean("is_active"));
		        vo.setStampEligible(rs.getBoolean("stamp_eligible"));
		        
		        list.add(vo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnManager.close(conn, ps, rs);
		}
		
		return list;
	}
	
	public List<ItemVO> searchActiveByText(String keyword) {
	    List<ItemVO> list = new ArrayList<>();
	    Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
	    try {
	        conn = ConnectionProvider.getConnection();

	        // 검색어 안전 처리: \, %, _ 이스케이프
	        String q = (keyword == null) ? "" : keyword;
	        q = q.replace("\\", "\\\\")   // \  -> \\
	             .replace("%", "\\%")     // %  -> \%
	             .replace("_", "\\_");    // _  -> \_

	        String sql =
	            "SELECT * FROM items " +
	            " WHERE is_active = TRUE " +
	            "   AND name LIKE ? " +
	            " ORDER BY type, name";

	        ps = conn.prepareStatement(sql);
	        ps.setString(1, "%" + q + "%");
	        rs = ps.executeQuery();

	        while (rs.next()) {
	            ItemVO vo = new ItemVO();
	            vo.setItemId(rs.getInt("item_id"));
	            vo.setType(ItemType.valueOf(rs.getString("type")));
	            vo.setName(rs.getString("name"));
	            vo.setPrice(rs.getInt("price"));
	            vo.setStock(rs.getInt("stock"));
	            vo.setActive(rs.getBoolean("is_active"));
	            vo.setStampEligible(rs.getBoolean("stamp_eligible"));
	            list.add(vo);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        DBConnManager.close(conn, ps, rs);
	    }
	    return list;
	}
}