package com.bkgoutchecker.api.service;

import com.bkgoutchecker.api.dto.UserDto;
import com.bkgoutchecker.api.dto.UserResponseDto;

public interface UserService {
    UserResponseDto getAllUser(int pageNo, int pageSize);

    UserDto getUserById(Long id);

    UserDto deleteUserId(Long id);
}
