package com.example.demo.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.Expense;
import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.Repository.ExpenseRepository;
import com.example.demo.Repository.CategoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")

public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseController(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/user/{userId}")
    public List<Expense> getExpensesByUser(@PathVariable Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        // بما أن الواجهة الأمامية أصبحت ذكية وترسل رقم المستخدم ورقم الفئة مع الطلب
        // لم نعد بحاجة للبحث عن الفئة، فقط نأمر قاعدة البيانات بالحفظ المباشر!
        return expenseRepository.save(expense);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
        } else {
            throw new RuntimeException("المصروف غير موجود أو تم حذفه مسبقاً!");
        }
    }

    // تعديل مصروف موجود
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @RequestBody Expense expenseDetails) {
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense != null) {
            expense.setAmount(expenseDetails.getAmount());
            expense.setExpenseDate(expenseDetails.getExpenseDate());
            if (expenseDetails.getCategory() != null) {
                expense.setCategory(expenseDetails.getCategory());
            }
            Expense updatedExpense = expenseRepository.save(expense);
            return ResponseEntity.ok(updatedExpense);
        }
        return ResponseEntity.badRequest().body("المصروف غير موجود!");
    }
}