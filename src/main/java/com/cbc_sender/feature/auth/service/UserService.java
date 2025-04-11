package com.cbc_sender.feature.auth.service;

import com.cbc_sender.feature.auth.dto.response.UserResponseDto;
import com.cbc_sender.feature.auth.dto.response.AllUserResponseDto;

public interface UserService {
    AllUserResponseDto getAllUser(int pageNo, int pageSize, String search);

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByToken();

    UserResponseDto deleteUserId(Long id);
}
