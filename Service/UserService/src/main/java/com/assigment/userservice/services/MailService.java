package com.assigment.userservice.services;

import com.assigment.userservice.configs.RabbitConfig;
import com.assigment.userservice.dto.response.ResetMessage;
import com.assigment.userservice.dto.response.VerificationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

@Service
public class MailService {

    private static final Logger logger = LogManager.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final View error;

    @Value("${app.verification.base-url:http://localhost:8090/auth/verify}")
    private String verificationBaseUrl;

    public MailService(JavaMailSender mailSender, View error) {
        this.mailSender = mailSender;
        this.error = error;
    }

    @RabbitListener(queues = RabbitConfig.VERIFICATION_QUEUE)
    public void handleVerification(VerificationMessage message) {
        try {
            String verificationLink = verificationBaseUrl + "?token=" + message.getToken();

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(message.getEmail());
            mailMessage.setSubject("Verify your account");
            mailMessage.setText("Click here to verify your account: " + verificationLink);

            mailSender.send(mailMessage);
            logger.info("📩 Sent verification email to {}", message.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to process verification message for {}: {}", message.getEmail(), e.getMessage(), e);
            throw e; // rethrow to keep visibility in logs
        }
    }

    @RabbitListener(queues = RabbitConfig.RESET_QUEUE)
    public void handlePasswordReset(ResetMessage message) {
        try {
            String resetLink = "http://localhost:8090/auth/verification/reset?token=" + message.getToken();

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(message.getEmail());
            mailMessage.setSubject("Password Reset Request");
            mailMessage.setText("Click here to reset your password: " + resetLink);

            mailSender.send(mailMessage);
            logger.info("📩 Sent password reset email to {}", message.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to process password reset message for {}: {}", message.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
