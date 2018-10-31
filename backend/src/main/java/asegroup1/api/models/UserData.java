package asegroup1.api.models;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_data")
public class UserData implements Serializable {

	public UserData() {
		super();
	}

	public UserData(String userId) {
		super();
		this.userId = userId;
	}

	@Id
	@Column(name = "USER_ID")
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
