package bokchi.java.model;

import bokchi.java.model.enums.ItemType;

public class ItemVO {
    private int itemId;                  // PK
    private ItemType type;               // DRINK, FOOD, GOODS 
    private String name;                 // 상품명
    private int price;                   // 가격
    private int stock;                   // 재고
    private boolean isActive;            // 판매 여부
    private String description;          // 상품 설명 (nullable)
    private boolean stampEligible;       // 적립 대상 여부

    public ItemVO() {}

    public ItemVO(int itemId, ItemType type, String name, int price,
                  int stock, boolean isActive, String description, boolean stampEligible) {
        this.itemId = itemId;
        this.type = type;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.isActive = isActive;
        this.description = description;
        this.stampEligible = stampEligible;
    }

    public int getItemId() {
        return itemId;
    }
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public ItemType getType() {
        return type;
    }
    public void setType(ItemType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStampEligible() {
        return stampEligible;
    }
    public void setStampEligible(boolean stampEligible) {
        this.stampEligible = stampEligible;
    }

	@Override
	public String toString() {
		return "ItemVO [itemId=" + itemId + ", type=" + type + ", name=" + name + ", price=" + price + ", stock="
				+ stock + ", isActive=" + isActive + ", description=" + description + ", stampEligible=" + stampEligible
				+ "]";
	}
}