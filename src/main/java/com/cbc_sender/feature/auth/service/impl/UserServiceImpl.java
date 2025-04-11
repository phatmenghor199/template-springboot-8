package com.cbc_sender.feature.auth.service.impl;

import com.cbc_sender.feature.auth.dto.response.UserResponseDto;
import com.cbc_sender.feature.auth.dto.response.AllUserResponseDto;
import com.cbc_sender.feature.auth.mapper.UserMapper;
import com.cbc_sender.feature.auth.models.UserEntity;
import com.cbc_sender.feature.auth.repository.UserRepository;
import com.cbc_sender.exceptions.error.NotFoundException;
import com.cbc_sender.feature.auth.service.UserService;
import com.cbc_sender.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AllUserResponseDto getAllUser(int pageNo, int pageSize, String search) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<UserEntity> userPage;
        if (search != null && !search.isEmpty()) {
            log.info("User is get and search by : {}", search);
            userPage = userRepository.findByUsernameContainingIgnoreCase(search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        List<UserResponseDto> content = userPage.getContent().stream().map(UserMapper::mapToDto).collect(Collectors.toList());

        return UserMapper.mapToListDto(content, userPage);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));
        return UserMapper.mapToDto(user);
    }

    @Override
    public UserResponseDto getUserByToken() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        return UserMapper.mapToDto(currentUser);
    }

    @Transactional
    @Override
    public UserResponseDto deleteUserId(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));
        user.getRoles().clear();
        userRepository.deleteById(id);
        return UserMapper.mapToDto(user);
    }
}
