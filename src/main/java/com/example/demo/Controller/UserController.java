package com.example.demo.Controller;

import com.example.demo.model.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // دالة لتحديث راتب المستخدم في قاعدة البيانات
    @PutMapping("/{id}/salary")
    public ResponseEntity<?> updateSalary(@PathVariable Long id, @RequestBody Map<String, Double> payload) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && payload.containsKey("salary")) {
            user.setSalary(payload.get("salary"));
            userRepository.save(user);
            return ResponseEntity.ok(user.getSalary());
        }
        return ResponseEntity.badRequest().body("حدث خطأ أثناء تحديث الراتب!");
    }

    // دالة لتحديث هدف الادخار للمستخدم
    @PutMapping("/{id}/goal")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            if (payload.containsKey("goalName")) {
                user.setGoalName((String) payload.get("goalName"));
            }
            if (payload.containsKey("goalAmount")) {
                // نستخدم هذه الطريقة لتجنب مشاكل تحويل الأرقام (Integer إلى Double)
                user.setGoalAmount(Double.valueOf(payload.get("goalAmount").toString()));
            }
            userRepository.save(user);
            return ResponseEntity.ok("تم حفظ الهدف بنجاح!");
        }
        return ResponseEntity.badRequest().body("حدث خطأ أثناء حفظ الهدف!");
    }

    // دالة لجلب بيانات المستخدم كاملة (الاسم، الراتب، والهدف)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("المستخدم غير موجود!");
    }

    // دالة لإضافة مبلغ للحصالة التراكمية (سواء إيداع يدوي أو ترحيل نهاية الشهر)
    @PutMapping("/{id}/savings/add")
    public ResponseEntity<?> addSavings(@PathVariable Long id, @RequestBody Map<String, Double> payload) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && payload.containsKey("amount")) {
            Double currentBalance = user.getSavedBalance() != null ? user.getSavedBalance() : 0.0;
            Double newBalance = currentBalance + payload.get("amount");
            user.setSavedBalance(newBalance);
            userRepository.save(user);
            return ResponseEntity.ok(newBalance);
        }
        return ResponseEntity.badRequest().body("حدث خطأ أثناء تحديث الحصالة!");
    }

    // دالة لتعديل رصيد الحصالة مباشرة (في حالة الخطأ أو سحب للطوارئ)
    @PutMapping("/{id}/savings/edit")
    public ResponseEntity<?> editSavingsBalance(@PathVariable Long id, @RequestBody Map<String, Double> payload) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && payload.containsKey("newBalance")) {
            Double newBalance = payload.get("newBalance");
            user.setSavedBalance(newBalance);
            userRepository.save(user);
            return ResponseEntity.ok(newBalance);
        }
        return ResponseEntity.badRequest().body("حدث خطأ أثناء تعديل الحصالة!");
    }
}