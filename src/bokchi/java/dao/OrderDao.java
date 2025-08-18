package bokchi.java.dao;

import bokchi.java.model.OrderVO;
import bokchi.java.model.enums.OrderStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface OrderDao {
	// 아이템 삽입
	void insertOrderItem(Connection conn, int orderId, int itemId, int qty, int unitPrice) throws SQLException;

	// 재고 차감 - stock >= qty 조건 시 차감, 아니면 false
	boolean decrementStockIfEnough(Connection conn, int itemId, int qty) throws SQLException;

	// 스탬프 적립 대상 여부
	boolean isStampEligible(Connection conn, int itemId) throws SQLException;

	// 주문 상태 변경
	void updateOrderStatus(Connection conn, int orderId, OrderStatus status) throws SQLException;

	// 오더 삽입
	int insertOrder(Connection conn, Integer userId, int totalAmount) throws SQLException;
	int insert(OrderVO order);

	// 아이디로 조회
	OrderVO findById(int orderId);

	// 전체조회
	List<OrderVO> findAll();

	// 유저아이디로 조회
	List<OrderVO> findByUserId(int userId);

	// 업데이트
	int updateStatus(int orderId, String status);
}