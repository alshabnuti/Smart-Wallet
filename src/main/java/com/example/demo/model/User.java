package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private double salary;
    private String goalName; // اسم الهدف
    private Double goalAmount; // المبلغ المطلوب للهدف
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore // تتجاهل القائمة أثناء تحويل المستخدم إلى JSON لمنع الدوران اللانهائي
    private List<Category> categories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore // تتجاهل القائمة أثناء تحويل المستخدم إلى JSON لمنع الدوران اللانهائي
    private List<Expense> expenses;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    // أضف هذا المتغير تحت حقل email
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // خطوة أمنية: تمنع إرسال كلمة المرور للواجهة عند جلب بيانات
                                                           // المستخدم
    private String password;

    // أضف دوال الـ Getters و Setters في الأسفل:
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public Double getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(Double goalAmount) {
        this.goalAmount = goalAmount;
    }

    @Column(columnDefinition = "double default 0.0")
    private Double savedBalance = 0.0; // رصيد الحصالة التراكمي

    // أضف هذه الدوال في أسفل الملف
    public Double getSavedBalance() {
        return savedBalance;
    }

    public void setSavedBalance(Double savedBalance) {
        this.savedBalance = savedBalance;

    }

    // أضف هذه الحقول في نهاية كلاس User
    private boolean active = false; // الحساب معطل افتراضياً
    private String verificationCode; // لتخزين كود التحقق

    // أضف Getters و Setters
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    // أضف هذا الحقل
    private String resetToken;

    // أضف الـ Getter والـ Setter
    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}