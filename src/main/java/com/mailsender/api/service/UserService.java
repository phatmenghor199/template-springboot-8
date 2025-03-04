package com.mailsender.api.service;

import com.mailsender.api.dto.UserDto;
import com.mailsender.api.dto.UserResponseDto;
import com.mailsender.api.request.ChangePasswordByAdminRequestDto;
import com.mailsender.api.request.ChangePasswordRequestDto;

public interface UserService {
    UserResponseDto getAllUser(int pageNo, int pageSize,String search);

    UserDto getUserById(Long id);

    UserDto getUserByToken();

    UserDto deleteUserId(Long id);

    UserDto changePassword(ChangePasswordRequestDto requestDto);
    UserDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto);
}
