package com.sutoga.backend.entity.mapper;

import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.UserDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User userDtoToUser(UserDto userDto);
    UserDto userToUserDto(User user);
    List<UserDto> userToUserDtoList(List<User> userList);
    List<User> userDtoToUserList(List<UserDto> userDtoList);
}
