package com.example.server.UsPinterest.controller;

import com.example.server.UsPinterest.dto.CategoryRequest;
import com.example.server.UsPinterest.dto.CategoryResponse;
import com.example.server.UsPinterest.model.Category;
import com.example.server.UsPinterest.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoriesController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> dtos = categoryService.getAllCategories().stream()
                .map(cat -> new CategoryResponse(cat.getId(), cat.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        Category saved = categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CategoryResponse(saved.getId(), saved.getName()));
    }
} 