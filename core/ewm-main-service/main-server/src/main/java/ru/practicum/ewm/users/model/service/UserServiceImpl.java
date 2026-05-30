package ru.practicum.ewm.users.model.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.users.dto.NewUserDto;
import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.mapper.UserMapper;
import ru.practicum.ewm.users.model.User;
import ru.practicum.ewm.users.repository.UserRepository;


import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public Collection<UserDto> getUsers(List<Long> ids, int from, int size) {
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
        } else {
            Page<User> page = userRepository.findAll(pageable);
            users = page.getContent();
        }

        return users.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(NewUserDto newUserDto) {
        validateUserCreation(newUserDto);
        User userEntity = userMapper.toEntity(newUserDto);
        User savedEntity = userRepository.save(userEntity);
        return userMapper.toUserDto(savedEntity);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        userRepository.delete(userRepository.getUserEntityById(userId));
    }

    @Override
    public void validateUserCreation(NewUserDto newUserDto) {
        if (newUserDto.getEmail() == null || newUserDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (newUserDto.getEmail().length() < 6 || newUserDto.getEmail().length() > 254) {
            throw new ValidationException("Длина email должна быть 6 - 254 символа");
        }

        if (newUserDto.getName() == null || newUserDto.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }

        if (newUserDto.getName().length() < 2 || newUserDto.getName().length() > 250) {
            throw new ValidationException("Длина имени должна быть 2 - 250 символов");
        }

        if (userRepository.existsByEmail(newUserDto.getEmail())) {
            throw new ConflictException("Пользователь уже зарегистрирован");
        }
    }
}

