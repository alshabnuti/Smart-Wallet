package com.example.demo.Controller;

import com.example.demo.model.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.EmailService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService; // 1. أضف هذا الحقل

    // 2. عدل الـ Constructor ليحتوي على emailService
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UserRepository userRepository, PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // 1. مسار إنشاء حساب جديد (Sign Up)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("البريد الإلكتروني مسجل مسبقاً!");
        }

        // توليد كود عشوائي من 6 أرقام
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        user.setVerificationCode(otp);
        user.setActive(false); // الحساب غير مفعل
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        // إرسال الإيميل
        emailService.sendVerificationEmail(user.getEmail(), otp);

        return ResponseEntity.ok("تم إنشاء الحساب بنجاح! يرجى التحقق من بريدك لإدخال رمز التفعيل.");
    }

    // 4. مسار التحقق من كود الـ OTP
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getVerificationCode().equals(otp)) {
                user.setActive(true);
                user.setVerificationCode(null); // مسح الكود بعد استخدامه
                userRepository.save(user);
                return ResponseEntity.ok("تم تفعيل الحساب بنجاح!");
            } else {
                return ResponseEntity.badRequest().body("رمز التحقق غير صحيح!");
            }
        }

        return ResponseEntity.badRequest().body("المستخدم غير موجود!");
    }

    // 2. مسار تسجيل الدخول (Sign In)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            String jwt = jwtUtil.generateToken(loginRequest.getEmail());
            User user = userRepository.findByEmail(loginRequest.getEmail()).get();

            return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getName()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("البريد الإلكتروني أو كلمة المرور غير صحيحة!");
        }
    }

    // 1. طلب استعادة كلمة السر
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("البريد الإلكتروني غير موجود!");
        }

        User user = userOpt.get();
        String token = String.format("%06d", new java.util.Random().nextInt(999999));

        user.setResetToken(token);
        userRepository.save(user);

        emailService.sendResetPasswordEmail(email, token);

        return ResponseEntity.ok("تم إرسال رمز الاستعادة إلى بريدك.");
    }

    // 2. تحديث كلمة السر
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (token.equals(user.getResetToken())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetToken(null); // مسح التوكين بعد الاستخدام
                userRepository.save(user);
                return ResponseEntity.ok("تم تحديث كلمة المرور بنجاح!");
            }
            return ResponseEntity.badRequest().body("رمز الاستعادة غير صحيح!");
        }
        return ResponseEntity.badRequest().body("المستخدم غير موجود!");
    }

    // 3. مسار تسجيل الدخول بحساب جوجل (Google Login) 🚀
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");

        try {
            // أ. إرسال الرمز إلى سيرفرات جوجل للتحقق منه
            RestTemplate restTemplate = new RestTemplate();
            String googleVerifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            ResponseEntity<Map> googleResponse = restTemplate.getForEntity(googleVerifyUrl, Map.class);

            if (googleResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> googleData = googleResponse.getBody();

                // ب. استخراج الإيميل والاسم من وثيقة جوجل الموثوقة
                String email = (String) googleData.get("email");
                String name = (String) googleData.get("name");

                // ج. البحث عن المستخدم في قاعدة بياناتنا باستخدام Optional
                Optional<User> userOpt = userRepository.findByEmail(email);
                User user;

                // د. إذا كان المستخدم جديداً تماماً، ننشئ له حساباً تلقائياً
                if (userOpt.isEmpty()) {
                    user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    // نضع كلمة مرور عشوائية ومشفرة تماماً لأنه سيستخدم حساب جوجل دائماً
                    user.setPassword(passwordEncoder.encode("Google_Oauth_" + UUID.randomUUID().toString()));
                    user = userRepository.save(user);
                } else {
                    user = userOpt.get(); // المستخدم موجود مسبقاً
                }

                // هـ. توليد تذكرة الدخول الخاصة بتطبيقنا (JWT) لهذا المستخدم
                String jwt = jwtUtil.generateToken(user.getEmail());

                // و. إرسال نفس الرد الذي تتوقعه شاشة Angular تماماً لينجح الدخول
                return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getName()));
            }

            return ResponseEntity.badRequest().body("رمز جوجل غير صالح أو منتهي الصلاحية!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("فشل التحقق من هوية جوجل: " + e.getMessage());
        }
    }

    // ==========================================
    // كلاسات مساعدة (Data Transfer Objects) لاستقبال وإرسال البيانات بشكل منظم
    // ==========================================

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class AuthResponse {
        private String token;
        private Long userId;
        private String name;

        public AuthResponse(String token, Long userId, String name) {
            this.token = token;
            this.userId = userId;
            this.name = name;
        }

        public String getToken() {
            return token;
        }

        public Long getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }
    }
}