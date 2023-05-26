package com.sutoga.backend.entity.dto;

import com.sutoga.backend.entity.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {

	Long id;
	String profilePhotoUrl;
	String userName;
	String email;
	String profileDescription;
	String firstName;
	String lastName;
	LocalDate birthDate;
	String phoneNumber;

	public UserResponse(User entity) {
		if (entity != null) {
			this.id = entity.getId();
			this.userName = entity.getUsername();
			this.email = entity.getEmail();
			this.profileDescription = entity.getProfileDescription();
			this.profilePhotoUrl = entity.getProfilePhotoUrl();
			this.firstName = entity.getFirstName();
			this.lastName = entity.getLastName();
			this.birthDate = entity.getBirthDate();
			this.phoneNumber = entity.getPhoneNumber();
		}
	}
}