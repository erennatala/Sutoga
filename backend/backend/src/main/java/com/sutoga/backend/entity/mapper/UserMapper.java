package com.sutoga.backend.entity.mapper;

import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "username", target = "userName")
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> userList);
}
