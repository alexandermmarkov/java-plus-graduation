package ru.practicum.ewm.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.core.exception.ConflictException;
import ru.practicum.ewm.core.exception.NotFoundException;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto create(@Valid CategoryDto dto) throws ConflictException {
        if (isCategoryNameDuplicate(dto)) {
            throw new ConflictException("Категория уже существует");
        }
        Category category = repository.save(mapper.toEntity(dto));
        log.info("Создана категория {}, id = {}", category.getName(), category.getId());
        return mapper.toDto(category);
    }

    @Transactional
    public CategoryDto update(Long catId, @Valid CategoryDto dto) throws ConflictException {
        var newDto = mapper.updateDto(getEntity(catId), dto);
        if (isCategoryNameDuplicate(newDto)) {
            throw new ConflictException("Категория уже существует");
        }
        var entity = repository.save(mapper.toEntity(newDto));
        log.info("Изменение категории OK");
        return mapper.toDto(entity);
    }

    @Transactional
    public void delete(Long catId) throws ConflictException {
        if (!categoryIsExist(catId)) {
            throw new NotFoundException("Удаляемая запись не найдена");
        }

//        Optional<Event> eventOpt = eventRepository.findFirstByCategoryId(catId);
//        if (eventOpt.isPresent()) {
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Категория с id = " + catId + " используется");
        }
        repository.deleteById(catId);
        log.info("Категория {} удалена", catId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> find(Pageable pageable) {
        return repository.findAll(pageable)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Boolean categoryIsExist(Long id) {
        return repository.existsById(id);
    }

    @Transactional(readOnly = true)
    public Boolean isCategoryNameDuplicate(CategoryDto dto) {
        return dto.getId() == null
                ? repository.existsByName(dto.getName())
                : repository.existsByNameAndIdNot(dto.getName(), dto.getId());
    }

    @Transactional(readOnly = true)
    public CategoryDto getEntity(Long userId) throws NotFoundException {
        log.info("Получить описание Категории. id {}", userId);
        return mapper.toDto(findById(userId));
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) throws NotFoundException {
        return repository.findById(id == null ? 0L : id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
    }


}
