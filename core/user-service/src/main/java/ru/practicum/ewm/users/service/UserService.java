package ru.practicum.ewm.users.service;

import ru.practicum.ewm.users.dto.NewUserDto;
import ru.practicum.ewm.users.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    Collection<UserDto> getUsers(List<Long> ids, int from, int size);

    UserDto createUser(NewUserDto newUserDto);

    void deleteUser(Long userId);

    void validateUserCreation(NewUserDto newUserDto);
}
