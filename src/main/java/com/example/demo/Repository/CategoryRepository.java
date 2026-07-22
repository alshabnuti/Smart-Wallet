package com.example.demo.Repository;

import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // <-- أضفنا هذا الاستيراد

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // الدالة التي أضفناها لجلب فئات المستخدم
    List<Category> findByUserId(Long userId);

    // الدالة الناقصة التي يبحث عنها الـ Controller
    Optional<Category> findByName(String name);
}