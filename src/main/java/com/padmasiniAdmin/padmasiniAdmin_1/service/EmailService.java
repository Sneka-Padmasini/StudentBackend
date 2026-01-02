package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException; // If this turns red, try: import javax.mail.MessagingException;
import jakarta.mail.internet.MimeMessage; // If red: import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // --- YOUR EXISTING OTP METHOD (Keep this) ---
    public void sendOtpEmail(String to, int otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP for verification is: " + otp);
        javaMailSender.send(message);
    }

    // --- ✅ ADD THIS NEW METHOD FOR STUDY TRACKER ---
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
 // --- ✅ UPDATED CONTACT METHOD (Uses your domain mail) ---
    public void sendContactMessageWithAttachment(String to, String subject, String body, MultipartFile file, String replyToEmail) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            // 1. FROM: Must match the email in your application.properties
            helper.setFrom("learnforward@padmasini.com"); 
            
            // 2. TO: Your support email
            helper.setTo(to);
            
            // 3. REPLY-TO: The Student's email (This makes the 'Reply' button work correctly)
            helper.setReplyTo(replyToEmail);

            helper.setSubject(subject);
            helper.setText(body);

            // If the user uploaded a file, attach it
            if (file != null && !file.isEmpty()) {
                helper.addAttachment(file.getOriginalFilename(), file);
            }

            javaMailSender.send(message);
            System.out.println("✅ Contact support email sent successfully from learnforward@padmasini.com");
            
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send contact email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    
 // --- ✅ UPDATED: Send Detailed Subscription Email ---
    public void sendSubscriptionSuccessEmail(String adminEmail, String userEmail, String userName, String plan, String paymentId, String contact, String amountPaid) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            // 1. Send to Admin
            message.setTo(adminEmail);
            message.setSubject("🔔 New Subscription Alert: " + plan.toUpperCase() + " - " + userName);
            message.setText("New Plan Activated!\n\n" +
                            "User Name: " + userName + "\n" +
                            "User Email: " + userEmail + "\n" +
                            "Contact: " + contact + "\n" +
                            "Plan: " + plan.toUpperCase() + "\n" +
                            "Amount Paid: ₹" + amountPaid + "\n" +
                            "Payment ID: " + paymentId + "\n" +
                            "Status: Payment Verified & Received.");
            
            javaMailSender.send(message);
            System.out.println("✅ Admin subscription alert sent.");

            // 2. Send Confirmation to User
            SimpleMailMessage userMsg = new SimpleMailMessage();
            userMsg.setTo(userEmail);
            userMsg.setSubject("Payment Successful - Padmasini Learning");
            userMsg.setText("Hello " + userName + ",\n\n" +
                            "We have successfully received your payment for the " + plan.toUpperCase() + " plan.\n" +
                            "Amount Paid: ₹" + amountPaid + "\n" +
                            "Payment ID: " + paymentId + "\n\n" +
                            "Your course access is now active. Thank you for learning with us!\n\n" +
                            "Team Padmasini");
            
            javaMailSender.send(userMsg);
            System.out.println("✅ User confirmation email sent.");

        } catch (Exception e) {
            System.err.println("❌ Failed to send subscription email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
