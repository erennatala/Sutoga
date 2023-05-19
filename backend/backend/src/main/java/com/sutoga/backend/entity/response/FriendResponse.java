package com.sutoga.backend.entity.response;

import lombok.Data;

@Data
public class FriendResponse {
    Long id;
    String username;
    String profilePhotoUrl;
    Boolean isFriend;
}
