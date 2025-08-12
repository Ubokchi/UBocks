package bokchi.java.dao;

import bokchi.java.model.RewardHistoryVO;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardHistoryDao {

	//기본 크루드
	int insert(RewardHistoryVO vo);
	RewardHistoryVO findById(int historyId);
	List<RewardHistoryVO> findAll();
	List<RewardHistoryVO> findByUserId(int userId);
	List<RewardHistoryVO> findByOrderId(int orderId);

	// ID로 시간 사이 검색
	List<RewardHistoryVO> findByUserIdBetween(int userId, LocalDateTime from, LocalDateTime to);
	// ID로 최근 검색
	List<RewardHistoryVO> findRecentByUserId(int userId, int limit);

	// +적립
	int recordEarn(int userId, Integer orderId, int stamps);     
	// -사용
	int recordRedeem(int userId, Integer orderId, int stampsUsed); 
	// 수동조정(메모 미사용 시 무시)
	int recordAdjust(int userId, int delta, String noteIgnored);   
}              