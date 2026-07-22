package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // مفتاح التشفير السري (يجب أن يكون طويلاً ومعقداً لتأمين التوكن)
    private final String SECRET_KEY = "my-super-secret-key-for-expense-app-which-is-very-long";

    // تحويل المفتاح النصي إلى صيغة تشفير معتمدة
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // استخراج البريد الإلكتروني (المعرف) من التوكن
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // استخراج تاريخ انتهاء صلاحية التوكن
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // توليد تذكرة مرور (Token) للمستخدم
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // التوكن صالح لمدة 24 ساعة (يمكنك تعديلها لاحقاً)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // التحقق من أن التوكن سليم ويخص هذا المستخدم ولم تنتهِ صلاحيته
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}