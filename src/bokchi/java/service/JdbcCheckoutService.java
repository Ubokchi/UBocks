package bokchi.java.service;

import bokchi.java.config.ConnectionProvider;
import bokchi.java.model.UserVO;
import bokchi.java.ui.user.CartPanel;

import java.sql.*;
import java.util.List;

public class JdbcCheckoutService implements CheckoutService {

    // 필요하면 프로젝트 공용 상수와 맞춰주세요
    public static final int FREE_DRINK_COST = 12; // RewardService.FREE_DRINK_COST 와 동일하게

    @Override
    public OrderResult checkout(UserVO customer,
                                List<CartPanel.CartLine> lines,
                                int uiTotalAmount,
                                boolean usedFreeDrink,
                                Integer freeDrinkItemId) throws SQLException {

        try (Connection conn = ConnectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1) 서버에서 총액 재계산(신뢰 원천)
                int total = 0;
                for (var l : lines) total += l.lineAmount;

                // 2) 주문 생성 (PENDING)
                int orderId;
                String sqlOrder = "INSERT INTO orders (user_id, status, total_amount) VALUES (?, 'PENDING', ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                    if (customer == null || customer.getUserId() == 0) ps.setNull(1, Types.INTEGER);
                    else ps.setInt(1, customer.getUserId());
                    ps.setInt(2, total);
                    if (ps.executeUpdate() != 1) throw new SQLException("주문 생성 실패");
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("order_id 생성 실패");
                        orderId = rs.getInt(1);
                    }
                }

                // 3) 라인 저장 + 재고 차감 + 적립 계산
                String sqlItem  = "INSERT INTO order_items (order_id, item_id, qty, unit_price) VALUES (?, ?, ?, ?)";
                String sqlStock = "UPDATE items SET stock = stock - ? WHERE item_id = ? AND stock >= ?";
                String sqlElig  = "SELECT stamp_eligible FROM items WHERE item_id = ?";

                int stampsEarned = 0;
                try (PreparedStatement psItem = conn.prepareStatement(sqlItem);
                     PreparedStatement psStock = conn.prepareStatement(sqlStock);
                     PreparedStatement psE    = conn.prepareStatement(sqlElig)) {
                    for (var l : lines) {
                        // order_items
                        psItem.clearParameters();
                        psItem.setInt(1, orderId);
                        psItem.setInt(2, l.itemId);
                        psItem.setInt(3, l.qty);
                        psItem.setInt(4, l.unitPrice);       // 무료라인은 0으로 들어옴
                        psItem.executeUpdate();

                        // 재고 차감 (낙관적 잠금)
                        psStock.clearParameters();
                        psStock.setInt(1, l.qty);
                        psStock.setInt(2, l.itemId);
                        psStock.setInt(3, l.qty);
                        if (psStock.executeUpdate() != 1) {
                            throw new SQLException("재고 부족/동시성 충돌: item_id=" + l.itemId);
                        }

                        // 적립 가능 (무료라인 제외)
                        psE.clearParameters();
                        psE.setInt(1, l.itemId);
                        try (ResultSet rs = psE.executeQuery()) {
                            boolean eligible = false;
                            if (rs.next()) eligible = rs.getBoolean(1);
                            if (eligible && l.unitPrice > 0) {
                                stampsEarned += l.qty; // 정책: 1잔당 1개
                            }
                        }
                    }
                }

                // 4) 무료 사용 시 차감 + history(REDEEM)
                if (usedFreeDrink && customer != null && customer.getUserId() != 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE users SET reward_balance = reward_balance - ? WHERE user_id = ? AND reward_balance >= ?")) {
                        ps.setInt(1, FREE_DRINK_COST);
                        ps.setInt(2, customer.getUserId());
                        ps.setInt(3, FREE_DRINK_COST);
                        if (ps.executeUpdate() != 1) throw new SQLException("스탬프 부족(무료 사용 실패)");
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'REDEEM')")) {
                        ps.setInt(1, customer.getUserId());
                        ps.setInt(2, orderId);
                        ps.setInt(3, -FREE_DRINK_COST);
                        ps.executeUpdate();
                    }
                }

                // 5) 적립 + history(EARN)
                if (customer != null && customer.getUserId() != 0 && stampsEarned > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE users SET reward_balance = reward_balance + ? WHERE user_id = ?")) {
                        ps.setInt(1, stampsEarned);
                        ps.setInt(2, customer.getUserId());
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO reward_history (user_id, order_id, delta, reason) VALUES (?, ?, ?, 'EARN')")) {
                        ps.setInt(1, customer.getUserId());
                        ps.setInt(2, orderId);
                        ps.setInt(3, stampsEarned);
                        ps.executeUpdate();
                    }
                }

                // 6) PAID로 마킹
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status='PAID' WHERE order_id = ?")) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                conn.commit();
                return new OrderResult(orderId, total, stampsEarned, usedFreeDrink);

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}