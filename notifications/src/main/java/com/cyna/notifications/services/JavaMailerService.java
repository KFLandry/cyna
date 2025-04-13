package com.cyna.notifications.services;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class JavaMailerService implements IEmailService    {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);

    }

    @Override
    public void sendEmail(String to, String subject, String text, String attachment) {

    }

    @Override
    public void sendEmail(String to, String subject, String text, String attachement, String templateType) {
        // TODO :  Create the templates, implemenent and test this method
        switch (templateType){
            case "signup":
                break;
            case "validate.email":
                break;
            case "delete.account":
                break;
            case "invoice.paid":
                break;
            case "invoice.payment_failed":
                break;
            case "invoice.finalized":
                break;
            case "customer.subscription.created":
                break;
            case "customer.subscription.deleted":
                break;
            case "customer.subscription.updated":
                break;
            default:
        }

    }
}
