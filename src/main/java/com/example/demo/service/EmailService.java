package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        // تأكد من وضع بريدك هنا
        message.setFrom("thunderxer199@gmail.com");
        message.setTo(toEmail);
        message.setSubject("رمز التحقق لتطبيق Smart Wallet");
        message.setText("مرحباً بك في Smart Wallet، رمز التحقق الخاص بك هو: " + otp);

        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("YOUR_EMAIL@gmail.com");
        message.setTo(toEmail);
        message.setSubject("استعادة كلمة المرور في Smart Wallet");
        message.setText("لقد طلبت استعادة كلمة المرور. رمز التحقق الخاص بك هو: " + token);
        mailSender.send(message);
    }
}