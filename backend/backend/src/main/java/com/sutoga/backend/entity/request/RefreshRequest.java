package com.sutoga.backend.entity.request;

import lombok.Data;

@Data
public class RefreshRequest {

	Long userId;
	String refreshToken;
}
