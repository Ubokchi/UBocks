package bokchi.java.dao;

import bokchi.java.model.UserVO;

import java.sql.Connection;
import java.sql.SQLException;

public interface UserDao {

    // 조회
    UserVO findByUsername(String username);
    UserVO findById(int userId);
    UserVO findByPhone(String phone);

    // 생성 / 수정 / 삭제
    int insert(UserVO vo) throws SQLException;
    int updateBasic(UserVO vo) throws SQLException;
    int delete(int userId) throws SQLException;

    // 리워드(스탬프) 관련 — 트랜잭션에서 쓰기 위해 Connection 받는 버전
    void addToRewardBalanceGuarded(Connection conn, int userId, int delta) throws SQLException;
    void addToRewardBalance(Connection conn, int userId, int delta) throws SQLException;
    int  getRewardBalance(Connection conn, int userId) throws SQLException;

    // (선택) Auto-connection 헬퍼 — 트랜잭션 외부 간단 호출용
    void addToRewardBalanceGuarded(int userId, int delta) throws SQLException;
    void addToRewardBalance(int userId, int delta) throws SQLException;
}