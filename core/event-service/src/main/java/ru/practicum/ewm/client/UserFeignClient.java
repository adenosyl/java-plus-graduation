package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.client.dto.UserDto;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserDto getUser(@PathVariable Long userId);
}