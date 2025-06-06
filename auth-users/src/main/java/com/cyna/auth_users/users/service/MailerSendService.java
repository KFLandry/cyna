package com.cyna.auth_users.users.service;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.Recipient;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MailerSendService implements IEmailService {

    @Value("${mailerSend.token}")
    private String token;

    @Value("${mailerSend.from}")
    private String from;

    @Value(value = "${mailerSend.support_email}")
    private String support_email;

    @Value("${mailerSend.templates.generic_template_with_button}")
    private String template;


    private MailerSend mailSender;
    private Email email;

    @PostConstruct
    public void init() {
        mailSender = new MailerSend();
        mailSender.setToken(token);
        this.email = new Email();
    }

    @Override
    public void sendEmail(String to, String link, String eventType) throws Exception {
        this.sendEmail(to, link, eventType, null);
    }

    public void sendEmail(String to, String link, String eventType,  String requester) throws Exception {

        email.setFrom("Cyna Team", from);
        Recipient recipient = new Recipient(to.split("@")[0], to);
        email.AddRecipient(recipient);
        email.setTemplateId(template);
        Map<String, String> button = new HashMap<>();
        String subject =  null;
        // Choix du contenu personnalisé en fonction du templateType
        switch (eventType) {
            case "signup":
                subject = "Welcome to Cyna Projet!";
                email.addPersonalization(recipient, "subject",subject );
                email.addPersonalization(recipient, "message", "Thank you for signing up with us. We're excited to have you on board. Please confirm your email to complete your registration.");
                // Button
                button.put("label", "Verify my email");
                break;
            case "validate.account":
                subject = "Account validation request";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "A new admin account has been created by "+ requester +", validate it by clicking the link provided if you were aware of this creation or ignore this mail .");
                // Button
                button.put("label", "Validate a new account");
                break;
            case "validate.email":
                subject = "Email Verification Required";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "To secure your account, please verify your email address by clicking the link provided.");
                // Button
                button.put("label", "Verify my email");
                break;
            case "password.forgot":
                subject = "Password forgot";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "Your password is forgotten.");


            default:
                log.error("[MailerSendService][sendEmail] {} are unhandled! ", eventType);
                break;
        }

        button.put("link", link);
        email.addPersonalization(recipient, "button", button);
        email.addPersonalization(recipient, "support_email", support_email);
        email.setSubject(subject);

        try {
            mailSender.emails().send(email);
        } catch (MailerSendException e) {
            log.error("[MailerSendService][sendMail] Error while sending email to {}", to, e);
            throw new MailerSendException("[MailerSendService][sendMail] Error while sending email to {}");
        }

    }
}