package bokchi.java.model;

public class OrderItemVO {
	private int orderItemId;   // PK
    private int orderId;       // FK(orders.order_id)
    private int itemId;        // FK(items.item_id)
    private int qty;           // 수량
    private int unitPrice;     // 단가
    
    public OrderItemVO() {}

	public OrderItemVO(int orderItemId, int orderId, int itemId, int qty, int unitPrice) {
		super();
		this.orderItemId = orderItemId;
		this.orderId = orderId;
		this.itemId = itemId;
		this.qty = qty;
		this.unitPrice = unitPrice;
	}

	public int getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(int orderItemId) {
		this.orderItemId = orderItemId;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}

	@Override
	public String toString() {
		return "OrderItemVO [orderItemId=" + orderItemId + ", orderId=" + orderId + ", itemId=" + itemId + ", qty="
				+ qty + ", unitPrice=" + unitPrice + "]";
	}
    
}
