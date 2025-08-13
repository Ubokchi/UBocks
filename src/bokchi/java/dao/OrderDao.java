package bokchi.java.dao;

import bokchi.java.model.OrderVO;
import java.util.List;

public interface OrderDao {

    // 생성
    int insert(OrderVO order);

    // 조회
    OrderVO findById(int orderId);
    List<OrderVO> findAll();
    List<OrderVO> findByUserId(int userId);

    // 상태 변경
    int updateStatus(int orderId, String status);
}