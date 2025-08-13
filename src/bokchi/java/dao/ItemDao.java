package bokchi.java.dao;

import bokchi.java.model.ItemVO;
import bokchi.java.model.enums.ItemType;

import java.util.List;

public interface ItemDao {
	
	int insert(ItemVO item);          
	List<ItemVO> findAll();                         
	int update(ItemVO item);                       
	int delete(int itemId);      
	
	// ID로 조회
	ItemVO findById(int itemId); 
	
	// 타입별 활성 상품 조회 - 판매 중
    List<ItemVO> findActiveByType(ItemType type);
    
    // 이름으로 상품 조회 - 판매 중
    List<ItemVO> searchActiveByText(String keyword);
}