package com.mailsender.api.service.impl;

import com.mailsender.api.dto.UserDto;
import com.mailsender.api.dto.UserResponseDto;
import com.mailsender.api.exceptions.BadRequestException;
import com.mailsender.api.exceptions.NotFoundException;
import com.mailsender.api.mapper.UserMapper;
import com.mailsender.api.models.UserEntity;
import com.mailsender.api.repository.UserRepository;
import com.mailsender.api.request.ChangePasswordByAdminRequestDto;
import com.mailsender.api.request.ChangePasswordRequestDto;
import com.mailsender.api.service.UserService;
import com.mailsender.api.utils.SecurityUtils;
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
    public UserResponseDto getAllUser(int pageNo, int pageSize, String search) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<UserEntity> userPage;
        if (search != null && !search.isEmpty()) {
            log.info("User is get and search by : {}", search);
            userPage = userRepository.findByUsernameContainingIgnoreCase(search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        List<UserDto> content = userPage.getContent().stream().map(UserMapper::mapToDto).collect(Collectors.toList());

        return UserMapper.mapToListDto(content, userPage);
    }

    @Override
    public UserDto getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));
        return UserMapper.mapToDto(user);
    }

    @Override
    public UserDto getUserByToken() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        return UserMapper.mapToDto(currentUser);
    }

    @Transactional
    @Override
    public UserDto deleteUserId(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));
        // userRepository.deleteByUserId(id);
        user.getRoles().clear();
        // userRepository.deleteById(id);
        userRepository.deleteById(id);
        return UserMapper.mapToDto(user);
    }

    @Override
    public UserDto changePassword(ChangePasswordRequestDto changePasswordDto) {
        UserEntity user = securityUtils.getCurrentUser();

        // Verify the current password
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        // Optionally, verify that new password and confirm password match
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirm password do not match.");
        }

        // Update the user's password
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        UserEntity userEntity = userRepository.save(user);
        return UserMapper.mapToDto(userEntity);
    }

    @Override
    public UserDto changePasswordByAdmin(ChangePasswordByAdminRequestDto changePasswordDto) {
        UserEntity user = userRepository.findById(changePasswordDto.getId())
                .orElseThrow(() -> {
                    log.error("User with id {} not found", changePasswordDto.getId());
                    return new NotFoundException("User with email " + changePasswordDto.getId() + " not found");
                });

        // Optionally, verify that new password and confirm password match
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirm password do not match.");
        }

        // Update the user's password
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        UserEntity userEntity = userRepository.save(user);
        return UserMapper.mapToDto(userEntity);
    }


}
