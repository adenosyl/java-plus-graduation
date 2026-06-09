package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto create(NewCategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }

        Category category = CategoryMapper.toEntity(dto);
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto update(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() ->
                        new NotFoundException("Category with id=" + catId + " was not found"));

        if (!category.getName().equalsIgnoreCase(dto.getName())
                && categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Category name must be unique");
        }

        category.setName(dto.getName());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void delete(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageRequest)
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long catId) {
        return categoryRepository.findById(catId)
                .map(CategoryMapper::toDto)
                .orElseThrow(() ->
                        new NotFoundException("Category with id=" + catId + " was not found"));
    }
}