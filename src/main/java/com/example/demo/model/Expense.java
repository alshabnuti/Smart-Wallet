package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;

    @Column(name = "expense_date", nullable = false)
    @JsonProperty("expense_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    // نتجاهل فقط حقل 'user' داخل كائن الفئة لمنع الدوران اللانهائي، مع السماح
    // لجاكسون بإرسال اسم الفئة ومعرفها للـ Frontend
    @JsonIgnoreProperties({ "user" })
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    // نتجاهل القوائم الدائرية داخل كائن المستخدم لإنهاء التكرار اللانهائي
    @JsonIgnoreProperties({ "expenses", "categories" })
    private User user;

    // وضع تاريخ اليوم تلقائياً إذا لم يتم إرساله من الواجهة الأمامية
    @PrePersist
    protected void onCreate() {
        if (this.expenseDate == null) {
            this.expenseDate = LocalDate.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}