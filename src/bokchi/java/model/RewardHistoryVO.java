package bokchi.java.model;

import java.time.LocalDateTime;

import bokchi.java.model.enums.RewardReason;

public class RewardHistoryVO {
	private int historyId;             // PK
    private int userId;                // FK(users.user_id)
    private Integer orderId;           // FK(orders.order_id), NULL 가능
    private int delta;                 // +적립 / -사용
    private RewardReason reason;       // EARN, REDEEM, ADJUST
    private LocalDateTime createdAt;   // 시각
    
    public RewardHistoryVO() {}

	public RewardHistoryVO(int historyId, int userId, Integer orderId, int delta, RewardReason reason,
			LocalDateTime createdAt) {
		super();
		this.historyId = historyId;
		this.userId = userId;
		this.orderId = orderId;
		this.delta = delta;
		this.reason = reason;
		this.createdAt = createdAt;
	}

	public int getHistoryId() {
		return historyId;
	}

	public void setHistoryId(int historyId) {
		this.historyId = historyId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

	public RewardReason getReason() {
		return reason;
	}

	public void setReason(RewardReason reason) {
		this.reason = reason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "RewardHistoryVO [historyId=" + historyId + ", userId=" + userId + ", orderId=" + orderId + ", delta="
				+ delta + ", reason=" + reason + ", createdAt=" + createdAt + "]";
	}
    
}
