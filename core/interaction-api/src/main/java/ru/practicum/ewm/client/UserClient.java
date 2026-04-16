package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/inner/users/list")
    List<UserDto> getUsers(@RequestParam List<Long> ids);

    @GetMapping("/inner/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

}