package com.bkgoutchecker.api.service.impl;

import com.bkgoutchecker.api.dto.UserDto;
import com.bkgoutchecker.api.dto.UserResponseDto;
import com.bkgoutchecker.api.exceptions.NotFoundException;
import com.bkgoutchecker.api.mapper.UserMapper;
import com.bkgoutchecker.api.models.UserEntity;
import com.bkgoutchecker.api.repository.UserRepository;
import com.bkgoutchecker.api.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDto getAllUser(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<UserEntity> user = userRepository.findAll(pageable);
        List<UserEntity> listOfUser = user.getContent();
        List<UserDto> content = listOfUser.stream().map(UserMapper::mapToDto).collect(Collectors.toList());

        return UserMapper.mapToListDto(content, user);
    }

    @Override
    public UserDto getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));
        return UserMapper.mapToDto(user);
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

}
