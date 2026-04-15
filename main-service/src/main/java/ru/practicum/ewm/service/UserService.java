package ru.practicum.ewm.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.core.exception.ConflictException;
import ru.practicum.ewm.core.exception.NotFoundException;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;


    @Transactional
    public UserDto create(@Valid UserDto userDto) throws ConflictException {
        log.info("Создать пользователя. email: {}", userDto.getEmail());
        if (isEmailExistsAnotherUser(userDto)) {
            throw new ConflictException("Адрес электронной почты уже используется");
        }
        User user = repository.save(mapper.toEntity(userDto));
        log.info("Создание пользователя OK, id = {}", user.getId());
        return mapper.toDto(user);
    }

    @Transactional
    public void delete(Long userId) {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Удаляемая запись не найдена");
        }
        repository.deleteById(userId);
        log.info("Удален пользователь id = {}", userId);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findUsers(List<Long> ids, Pageable pageable) {
        var result = (CollectionUtils.isEmpty(ids))
                ? repository.findAll(pageable).stream().toList()
                : repository.findAllById(ids);
        return result.stream()
                .map(mapper::toDto)
                .toList();

    }

    @Transactional(readOnly = true)
    public Boolean isEmailExistsAnotherUser(UserDto userDto) {
        return Optional.ofNullable(userDto.getId())
                .map(id -> repository.existsByEmailAndIdNot(userDto.getEmail(), id))
                .orElseGet(() -> repository.existsByEmail(userDto.getEmail()));
    }

    @Transactional(readOnly = true)
    public Boolean userIsExist(Long userId) {
        return repository.existsById(userId);
    }
}
