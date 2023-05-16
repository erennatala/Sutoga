package com.sutoga.backend.entity.response;

import lombok.Data;

@Data
public class FriendRequestResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
}