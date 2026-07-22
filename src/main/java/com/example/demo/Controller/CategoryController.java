package com.example.demo.Controller;

import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryController(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // 1. جلب جميع الفئات الخاصة بمستخدم معين
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getUserCategories(@PathVariable Long userId) {
        List<Category> categories = categoryRepository.findByUserId(userId);
        return ResponseEntity.ok(categories);
    }

    // 2. إضافة فئة جديدة (نوع مصروف جديد)
    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody Category category) {
        // التأكد من أن الفئة مرتبطة بمستخدم حقيقي
        if (category.getUser() != null && category.getUser().getId() != null) {
            User user = userRepository.findById(category.getUser().getId()).orElse(null);
            if (user != null) {
                category.setUser(user); // ربط الفئة بالمستخدم
                Category savedCategory = categoryRepository.save(category);
                return ResponseEntity.ok(savedCategory);
            }
        }
        return ResponseEntity.badRequest().body("بيانات المستخدم غير صحيحة!");
    }

    // 3. حذف فئة
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok("تم حذف الفئة بنجاح!");
    }

    // 4. تعديل فئة موجودة (الاسم أو المبلغ المخصص)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            category.setName(categoryDetails.getName());
            category.setAllocated(categoryDetails.getAllocated());
            Category updatedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(updatedCategory);
        }
        return ResponseEntity.badRequest().body("الفئة غير موجودة!");
    }
}