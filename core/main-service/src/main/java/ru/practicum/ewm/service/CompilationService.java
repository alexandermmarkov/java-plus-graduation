package ru.practicum.ewm.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.core.exception.ConditionsException;
import ru.practicum.ewm.core.exception.NotFoundException;
import ru.practicum.ewm.core.interfaceValidation.CreateValidation;
import ru.practicum.ewm.core.interfaceValidation.UpdateValidation;
import ru.practicum.ewm.dto.compilation.CompilationFullDto;
import ru.practicum.ewm.dto.compilation.CompilationUpdateDto;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class CompilationService {

    private final CompilationRepository repository;
    private final CompilationMapper mapper;

    private final EventRepository eventRepository;


    @Transactional
    @Validated(CreateValidation.class)
    public CompilationFullDto create(@Valid CompilationUpdateDto dto) throws ConditionsException {
        Set<Event> events = new HashSet<>(getUniqueEvents(dto.getEvents()));
        Compilation compilation = repository.save(mapper.toEntity(dto, events));
        log.info("Создана подборка, id = {}", compilation.getId());
        return mapper.toFullDto(compilation);
    }

    @Transactional
    public void delete(Long compId) {
        if (!compilationIsExist(compId)) {
            throw new NotFoundException("Подборка с id " + compId + " не найдена");
        }
        repository.deleteById(compId);
        log.info("Удалена подборка id = {}", compId);
    }


    @Transactional
    @Validated(UpdateValidation.class)
    public CompilationFullDto update(Long compId, @Valid CompilationUpdateDto dto) throws ConditionsException {
        var compilation = findById(compId);

        Set<Event> events = new HashSet<>(getUniqueEvents(dto.getEvents()));
        compilation = mapper.toEntityGeneral(compilation, dto, events);
        log.info("Обновлена подборка id = {}", compId);
        return mapper.toFullDto(compilation);
    }

    @Transactional(readOnly = true)
    public Compilation findById(Long id) throws NotFoundException {
        return repository.findById(id == null ? 0L : id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
    }

    @Transactional(readOnly = true)
    public List<CompilationFullDto> find(Boolean pinned, Pageable pageable) {
        var page = (pinned != null)
                ? repository.findAllByPinned(pinned, pageable)
                : repository.findAll(pageable);

        return page.getContent()
                .stream()
                .map(mapper::toFullDto)
                .toList();
    }

    private List<Event> getUniqueEvents(List<Long> eventsByDto) throws ConditionsException {
        List<Event> events = new ArrayList<>();
        if (eventsByDto != null && !eventsByDto.isEmpty()) {
            Set<Long> unique = new HashSet<>(eventsByDto);
            log.info("Проверка уникальности events");
            if (unique.size() != eventsByDto.size()) {
                throw new ConditionsException("Список событий содержит дубликаты");
            }
            log.info("Сбор events для заполнения");
            events = eventRepository.findAllById(eventsByDto);
            if (events.size() != eventsByDto.size()) {
                throw new NotFoundException("Некоторые события не найдены");
            }
        }
        return events;
    }

    @Transactional(readOnly = true)
    public Boolean compilationIsExist(Long id) {
        return repository.existsById(id);
    }

    @Transactional(readOnly = true)
    public CompilationFullDto getEntityFool(Long compId) {
        return mapper.toFullDto(findById(compId));
    }
}
