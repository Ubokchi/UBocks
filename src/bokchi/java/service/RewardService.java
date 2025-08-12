package bokchi.java.service;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.config.DBConnManager;
import bokchi.java.model.enums.RewardReason;

import java.sql.*;

public class RewardService {
    /** 무료 음료 1잔에 필요한 스탬프 수 */
    public static final int FREE_DRINK_COST = 12;

    // =========================
    // 간편 호출(내부에서 커넥션 열고 닫음)
    // =========================
    public void earnStamps(int userId, Integer orderId, int stamps) {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);
            earnStamps(conn, userId, orderId, stamps);
            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            DBConnManager.close(conn, (PreparedStatement) null);
        }
    }

    public void redeemStamps(int userId, Integer orderId, int stampsToUse) {
        Connection conn = null;
        try {
            conn = ConnectionProvider.getConnection();
            conn.setAutoCommit(false);
            redeemStamps(conn, userId, orderId, stampsToUse);
            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            DBConnManager.close(conn, (PreparedStatement) null);
        }
    }

    public void redeemFreeDrink(int userId, Integer orderId) {
        redeemStamps(userId, orderId, FREE_DRINK_COST);
    }

    // =========================
    // 트랜잭션 연동형(외부 conn 사용)
    // =========================

    /** 동일 트랜잭션 내에서 스탬프 적립 */
    public void earnStamps(Connection conn, int userId, Integer orderId, int stamps) throws SQLException {
        if (stamps <= 0) return;

        // users.reward_balance += stamps
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET reward_balance = reward_balance + ? WHERE user_id = ?")) {
            ps.setInt(1, stamps);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            if (updated != 1) throw new SQLException("유저를 찾을 수 없습니다. user_id=" + userId);
        }

        // reward_history INSERT (EARN)
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, userId);
            if (orderId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, orderId);
            ps.setInt(3, stamps);
            ps.setString(4, RewardReason.EARN.name());
            ps.executeUpdate();
        }
    }

    /** 동일 트랜잭션 내에서 스탬프 사용 */
    public void redeemStamps(Connection conn, int userId, Integer orderId, int stampsToUse) throws SQLException {
        if (stampsToUse <= 0) return;

        int balance = getCurrentBalance(conn, userId);
        if (balance < stampsToUse) {
            throw new SQLException("스탬프가 부족합니다. 보유=" + balance + ", 필요=" + stampsToUse);
        }

        // users.reward_balance -= stampsToUse
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET reward_balance = reward_balance - ? WHERE user_id = ?")) {
            ps.setInt(1, stampsToUse);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            if (updated != 1) throw new SQLException("유저를 찾을 수 없습니다. user_id=" + userId);
        }

        // reward_history INSERT (REDEEM, delta는 음수)
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, userId);
            if (orderId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, orderId);
            ps.setInt(3, -Math.abs(stampsToUse));
            ps.setString(4, RewardReason.REDEEM.name());
            ps.executeUpdate();
        }
    }

    /** 동일 트랜잭션 내에서 무료 음료 처리(12개 차감) */
    public void redeemFreeDrink(Connection conn, int userId, Integer orderId) throws SQLException {
        redeemStamps(conn, userId, orderId, FREE_DRINK_COST);
    }

    /** 현재 스탬프 잔액 조회 (동일 커넥션에서 읽기) */
    public int getCurrentBalance(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT reward_balance FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("유저를 찾을 수 없습니다. user_id=" + userId);
    }

    /** 보유 스탬프로 무료음료 가능? */
    public boolean canRedeemFreeDrink(int currentBalance) {
        return currentBalance >= FREE_DRINK_COST;
    }
}