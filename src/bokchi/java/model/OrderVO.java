package bokchi.java.model;

import java.time.LocalDateTime;

import bokchi.java.model.enums.OrderStatus;

public class OrderVO {
	private int orderId;                 // PK
    private Integer userId;              // FK(users.user_id), NULL 가능
    private LocalDateTime orderTime;     // 주문 시각
    private OrderStatus status;          // PENDING, PAID, CANCELLED, COMPLETED
    private int totalAmount;             // 총액
    
    public OrderVO() {}

	public OrderVO(int orderId, Integer userId, LocalDateTime orderTime, OrderStatus status, int totalAmount) {
		super();
		this.orderId = orderId;
		this.userId = userId;
		this.orderTime = orderTime;
		this.status = status;
		this.totalAmount = totalAmount;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public LocalDateTime getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(LocalDateTime orderTime) {
		this.orderTime = orderTime;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public int getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public String toString() {
		return "OrderItemVO [orderId=" + orderId + ", userId=" + userId + ", orderTime=" + orderTime + ", status="
				+ status + ", totalAmount=" + totalAmount + "]";
	}
}
