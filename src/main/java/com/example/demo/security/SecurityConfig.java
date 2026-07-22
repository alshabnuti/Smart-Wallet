package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // الحل هنا: دمج إعدادات الـ CORS مع جدار الحماية
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated() // نكتبها مرة واحدة فقط في النهاية
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // إعدادات الـ CORS الاحترافية للسماح للواجهة الأمامية وتطبيق الهاتف بالاتصال
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // التعديل السحري هنا: استخدمنا AllowedOriginPatterns بدلاً من AllowedOrigins
        // للسماح للجميع
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // السماح بجميع أنواع الطلبات
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // السماح بإرسال الرؤوس (Headers)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
     * // إعدادات الـ CORS الاحترافية للسماح للواجهة الأمامية بالاتصال
     * 
     * @Bean
     * public CorsConfigurationSource corsConfigurationSource() {
     * CorsConfiguration configuration = new CorsConfiguration();
     * // السماح لتطبيق Angular الخاص بك بالاتصال
     * configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
     * // السماح بجميع أنواع الطلبات (إضافة، جلب، حذف، والطلب الخفي OPTIONS)
     * configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE",
     * "OPTIONS"));
     * // السماح بإرسال رأس الطلب الذي يحتوي على التذكرة
     * configuration.setAllowedHeaders(Arrays.asList("Authorization",
     * "Content-Type"));
     * configuration.setAllowCredentials(true);
     * 
     * UrlBasedCorsConfigurationSource source = new
     * UrlBasedCorsConfigurationSource();
     * source.registerCorsConfiguration("/**", configuration);
     * return source;
     * }
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}