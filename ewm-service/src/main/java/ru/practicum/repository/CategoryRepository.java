package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Category;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
