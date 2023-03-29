package com.sutoga.backend.entity.dto;

import com.sutoga.backend.entity.User;
import lombok.Data;

@Data
public class UserResponse {
	
	Long id;
	int avatarId;
	String userName;

	public UserResponse(User entity) {
		this.id = entity.getId();
		this.userName = entity.getUsername();
	} 
}
