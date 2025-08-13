package bokchi.java.dao;

import bokchi.java.model.RewardHistoryVO;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardHistoryDao {

    // 기본 CRUD
    int insert(RewardHistoryVO vo);
    RewardHistoryVO findById(int historyId);
    List<RewardHistoryVO> findAll();
    List<RewardHistoryVO> findByUserId(int userId);
    List<RewardHistoryVO> findByOrderId(int orderId);

    // 기간 / 최근
    List<RewardHistoryVO> findByUserIdBetween(int userId, LocalDateTime from, LocalDateTime to);
    List<RewardHistoryVO> findRecentByUserId(int userId, int limit);

    // 기록 헬퍼
    int recordEarn(int userId, Integer orderId, int stamps);      // +적립
    int recordRedeem(int userId, Integer orderId, int stampsUsed); // -사용
    int recordAdjust(int userId, int delta, String noteIgnored);   // 수동 조정
}