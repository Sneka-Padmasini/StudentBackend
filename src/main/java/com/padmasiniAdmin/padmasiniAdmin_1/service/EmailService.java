package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send a simple OTP email to the recipient.
     * @param to recipient email
     * @param otp the numeric otp
     */
    public void sendOtpEmail(String to, int otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Padmasini OTP Code");
        message.setText("Your OTP code is: " + otp + "\n\nThis code is valid for 5 minutes.\n\nIf you didn't request this, ignore this email.");
        // Optionally setFrom if you want a custom sender:
        // message.setFrom("noreply@padmasini.com");
        mailSender.send(message);
    }
}
