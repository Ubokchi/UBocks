package bokchi.java.dao;

import bokchi.java.model.RewardHistoryVO;
import java.util.List;

public interface RewardHistoryDao {
	// 리워드 내역 추가
	int insert(RewardHistoryVO history);            

	// 특정 유저 리워드 내역 조회
	List<RewardHistoryVO> findByUserId(int userId); 
	// 전체 리워드 내역 조회
	List<RewardHistoryVO> findAll();                
}