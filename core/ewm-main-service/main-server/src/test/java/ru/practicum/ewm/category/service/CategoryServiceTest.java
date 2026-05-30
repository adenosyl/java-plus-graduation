package ru.practicum.ewm.category.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    void createCategory_success() {
        NewCategoryDto dto = new NewCategoryDto("Music");

        CategoryDto result = categoryService.create(dto);

        assertNotNull(result.getId());
        assertEquals("Music", result.getName());
    }

    @Test
    void getById_success() {
        CategoryDto created = categoryService.create(new NewCategoryDto("Cinema"));

        CategoryDto found = categoryService.getById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Cinema", found.getName());
    }

    @Test
    void deleteCategory_success() {
        CategoryDto created = categoryService.create(new NewCategoryDto("Sport"));

        categoryService.delete(created.getId());

        assertThrows(NotFoundException.class,
                () -> categoryService.getById(created.getId()));
    }
}
