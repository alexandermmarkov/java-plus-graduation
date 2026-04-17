package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public List<UserDto> getUsers(List<Long> ids) {
        log.info("вызван UserClientFallback getUsers");
        return ids.stream()
                .map(id -> new UserDto(id, "Пользователь недоступен", null))
                .toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("вызван UserClientFallback getUserById");
        return new UserDto(id, "Unknown", null);
    }
}
