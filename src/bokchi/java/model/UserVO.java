package bokchi.java.model;

import java.time.LocalDateTime;

import bokchi.java.model.enums.Role;

public class UserVO {
    private int userId;                 // PK
    private String username;            // STAFF 전용 로그인 ID
    private String password;            // STAFF 전용 비밀번호
    private Role role;                  // STAFF or CUSTOMER
    private String phone;               // 고객 전화번호
    private String name;                // 이름
    private int rewardBalance;          // 보유 리워드(스탬프)
    private LocalDateTime createdAt;    // 생성일
    
    public UserVO() {}

	public UserVO(int userId, String username, String password, Role role, String phone, String name, int rewardBalance,
			LocalDateTime createdAt) {
		super();
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.role = role;
		this.phone = phone;
		this.name = name;
		this.rewardBalance = rewardBalance;
		this.createdAt = createdAt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRewardBalance() {
		return rewardBalance;
	}

	public void setRewardBalance(int rewardBalance) {
		this.rewardBalance = rewardBalance;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "UserVO [userId=" + userId + ", username=" + username + ", password=" + password + ", role=" + role
				+ ", phone=" + phone + ", name=" + name + ", rewardBalance=" + rewardBalance + ", createdAt="
				+ createdAt + "]";
	}
}
