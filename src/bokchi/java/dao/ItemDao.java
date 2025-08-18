package bokchi.java.dao;

import bokchi.java.model.ItemVO;
import bokchi.java.model.enums.ItemType;

import java.util.List;

public interface ItemDao {
	// 상품 추가
	int insert(ItemVO item);

	// ID로 검색
	ItemVO findById(int itemId);

	// 전체 조회
	List<ItemVO> findAll();

	// 상품 수정
	int update(ItemVO item);

	// 상품 삭제
	int delete(int itemId);

	// 타입별 활성 상품 조회 - 판매 중
	List<ItemVO> findActiveByType(ItemType type);
	List<ItemVO> searchActiveByText(String keyword);
}