package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.core.config.CommonMapperConfiguration;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.model.User;

@Mapper(config = CommonMapperConfiguration.class)
public interface UserMapper {

    UserDto toDto(User entity);

    @Mapping(target = "email", ignore = true)
    UserDto toUserDtoShort(User entity);

    User toEntity(UserDto dto);

}
