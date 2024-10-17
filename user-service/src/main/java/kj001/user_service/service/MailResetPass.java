package kj001.user_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kj001.user_service.dtos.MailEntity;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class MailResetPass {
    private final JavaMailSender javaMailSender;

    @Value("spring.mail.username")
    private String sender;
    public void sendMailOTP(MailEntity mailEntity) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(new InternetAddress(sender, "Trip Planner"));
        mimeMessageHelper.setTo(mailEntity.getEmail());
        mimeMessageHelper.setSubject(mailEntity.getSubject());
        mimeMessageHelper.setText(mailEntity.getContent(), true);
        javaMailSender.send(mimeMessage);
    }
}
