package com.mailsender.api.mapper;

import com.mailsender.api.dto.UserDto;
import com.mailsender.api.dto.UserResponseDto;
import com.mailsender.api.models.Role;
import com.mailsender.api.models.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDto mapToDto(UserEntity user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getUsername());

        String roles = user.getRoles().stream()
                .map(Role::getName) // Assuming 'getName()' gets the role name
                .collect(Collectors.joining(", "));
        userDto.setUserRole(roles);

        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        return userDto;
    }

    public static UserResponseDto mapToListDto(List<UserDto> content, Page<UserEntity> user) {
        UserResponseDto userResponse = new UserResponseDto();
        userResponse.setContent(content);
        userResponse.setPageNo(user.getNumber() + 1);
        userResponse.setPageSize(user.getSize());
        userResponse.setTotalElements(user.getTotalElements());
        userResponse.setTotalPages(user.getTotalPages());
        userResponse.setLast(user.isLast());
        return userResponse;
    }
}
