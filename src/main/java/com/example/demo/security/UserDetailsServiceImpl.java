package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // هذه الدالة هي "المترجم". يستدعيها Spring Security تلقائياً عند تسجيل الدخول
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. نبحث عن المستخدم في قاعدة البيانات باستخدام الإيميل (Email)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("المستخدم غير موجود: " + email));

        // 2. نترجم بيانات المستخدم إلى كائن قياسي يفهمه Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>() // هنا نضع الصلاحيات (مثل Admin أو User)، سنتركها فارغة حالياً
        );
    }
}