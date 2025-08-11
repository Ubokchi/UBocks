package bokchi.java.dao;

import bokchi.java.model.UserVO;
import java.util.List;

public interface UserDao {
	// 회원 추가
	int insert(UserVO user);                       

	// username으로 검색
	UserVO findByUsername(String username);        
	// ID로 검색
	UserVO findById(int userId);                    

	// 전체 조회
	List<UserVO> findAll();                        

	// 회원 정보 수정
	int update(UserVO user);                        

	// 회원 삭제
	int delete(int userId);                         
}