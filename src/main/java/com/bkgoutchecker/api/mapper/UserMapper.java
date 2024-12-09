package com.bkgoutchecker.api.mapper;

import com.bkgoutchecker.api.dto.UserDto;
import com.bkgoutchecker.api.dto.UserResponseDto;
import com.bkgoutchecker.api.models.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public class UserMapper {
    public static UserDto mapToDto(UserEntity user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        return userDto;
    }

    public static UserResponseDto mapToListDto(List<UserDto> content, Page<UserEntity> user) {
        UserResponseDto userResponse = new UserResponseDto();
        userResponse.setContent(content);
        userResponse.setPageNo(user.getNumber());
        userResponse.setPageSize(user.getSize());
        userResponse.setTotalElements(user.getTotalElements());
        userResponse.setTotalPages(user.getTotalPages());
        userResponse.setLast(user.isLast());
        return userResponse;
    }
}
