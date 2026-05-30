package ru.practicum.ewm.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.users.dto.NewUserDto;
import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserDto newUserDto);

    UserDto toUserDto(User userEntity);
}