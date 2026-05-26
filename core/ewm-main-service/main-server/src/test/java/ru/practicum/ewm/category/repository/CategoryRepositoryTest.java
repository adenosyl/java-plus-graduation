package ru.practicum.ewm.category.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.category.model.Category;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindCategory() {
        Category category = new Category();
        category.setName("Музыка");

        Category saved = categoryRepository.save(category);

        assertThat(saved.getId()).isNotNull();

        Category found = categoryRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Музыка");
    }

    @Test
    void shouldCheckExistsByNameIgnoreCase() {
        Category category = new Category();
        category.setName("Спорт");

        categoryRepository.save(category);

        boolean exists = categoryRepository.existsByNameIgnoreCase("спорт");

        assertThat(exists).isTrue();
    }
}