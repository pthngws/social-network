package com.phithang.mysocialnetwork.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    // Sinh mã OTP ngẫu nhiên (6 chữ số)
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // Tạo số từ 100000 đến 999999
        return String.valueOf(otp);
    }

    // Lưu OTP vào Redis với thời gian sống (TTL) là 5 phút
    public void saveOtp(String email, String otp) {
        redisTemplate.opsForValue().set("OTP:" + email, otp, 5, TimeUnit.MINUTES);
    }

    // Gửi email chứa OTP cho đăng ký
    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Xác nhận OTP đăng ký");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã này có hiệu lực trong 5 phút.");
        mailSender.send(message);
    }

    // Gửi email chứa OTP cho quên mật khẩu
    public void sendOtpForPasswordReset(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Xác nhận OTP đặt lại mật khẩu");
        message.setText("Mã OTP để đặt lại mật khẩu của bạn là: " + otp + ". Mã này có hiệu lực trong 5 phút.");
        mailSender.send(message);
    }

    // Xác nhận OTP
    public boolean verifyOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP:" + email);
        return storedOtp != null && storedOtp.equals(otp);
    }

    // Xóa OTP sau khi xác nhận thành công
    public void deleteOtp(String email) {
        redisTemplate.delete("OTP:" + email);
    }
}