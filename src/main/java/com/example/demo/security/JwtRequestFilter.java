package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    // حقن (إدخال) المترجم ومصنع التذاكر لكي يستخدمهم الحارس
    public JwtRequestFilter(UserDetailsServiceImpl userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    // هذه الدالة هي نقطة التفتيش الفعلية التي تعمل مع كل طلب
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. نبحث عن التذكرة في رأس الطلب (Header) تحت مسمى "Authorization"
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. التذكرة الصالحة تبدأ دائماً بكلمة "Bearer " (حامل التذكرة)
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // نقص كلمة Bearer لنستخرج التذكرة الصافية
            try {
                username = jwtUtil.extractUsername(jwt); // نستخرج الإيميل من التذكرة
            } catch (Exception e) {
                System.out.println("تذكرة غير صالحة أو منتهية!");
            }
        }

        // 3. إذا وجدنا الإيميل ولم يكن المستخدم مسجل الدخول مسبقاً في هذا الطلب
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // نجلب بيانات المستخدم من قاعدة البيانات عبر "المترجم"
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // نتأكد أن التذكرة سليمة 100%
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // إذا كانت سليمة، نعطيه "تصريح دخول رسمي" داخل السيرفر
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        // 4. أخيراً، نسمح للطلب بمتابعة طريقه سواء كان معه تذكرة أو لا (لأن هناك مسارات
        // سنسمح بها للجميع مثل التسجيل)
        chain.doFilter(request, response);
    }
}