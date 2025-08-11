package bokchi.java.dao;

import bokchi.java.model.OrderVO;
import java.util.List;

public interface OrderDao {
	// 주문 추가
	int insert(OrderVO order);                      

	// 주문 ID로 검색
	OrderVO findById(int orderId);                  

	// 전체 주문 조회
	List<OrderVO> findAll();                     
	// 특정 유저 주문 조회
	List<OrderVO> findByUserId(int userId);         

	// 주문 상태 변경
	int updateStatus(int orderId, String status);   
}