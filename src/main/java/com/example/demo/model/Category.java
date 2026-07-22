package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name")
    private String name; // أزلنا الوسم من هنا لتجنب أي تضارب

    private double allocated;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "categories", "expenses" })
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // هنا الحل الجذري: إجبار Spring Boot على قراءة وإرسال هذا المسمى
    @JsonProperty("category_name")
    public String getName() {
        return name;
    }

    @JsonProperty("category_name")
    public void setName(String name) {
        this.name = name;
    }

    public double getAllocated() {
        return allocated;
    }

    public void setAllocated(double allocated) {
        this.allocated = allocated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}