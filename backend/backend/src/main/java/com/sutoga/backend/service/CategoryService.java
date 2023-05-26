package com.sutoga.backend.service;

import com.sutoga.backend.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Category createCategory(Category category);

    Optional<Category> getCategoryById(Long categoryId);

    List<Category> getAllCategories();

    void deleteCategory(Long categoryId);
}
