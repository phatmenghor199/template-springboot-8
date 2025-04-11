package com.cbc_sender.feature.auth.mapper;

import com.cbc_sender.feature.auth.dto.response.UserResponseDto;
import com.cbc_sender.feature.auth.dto.response.AllUserResponseDto;
import com.cbc_sender.feature.auth.models.Role;
import com.cbc_sender.feature.auth.models.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserResponseDto mapToDto(UserEntity user) {
        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getUsername());

        String roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(", "));
        userDto.setUserRole(roles);

        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        return userDto;
    }

    public static AllUserResponseDto mapToListDto(List<UserResponseDto> content, Page<UserEntity> user) {
        AllUserResponseDto userResponse = new AllUserResponseDto();
        userResponse.setContent(content);
        userResponse.setPageNo(user.getNumber() + 1);
        userResponse.setPageSize(user.getSize());
        userResponse.setTotalElements(user.getTotalElements());
        userResponse.setTotalPages(user.getTotalPages());
        userResponse.setLast(user.isLast());
        return userResponse;
    }
}
