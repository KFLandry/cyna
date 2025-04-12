package com.cyna.subscriptions.services;

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
    public void sendEmail(String to, String link, String eventType) {

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
            case "validate.email":
                subject = "Email Verification Required";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "To secure your account, please verify your email address by clicking the link provided.");
                // Button
                button.put("label", "Verify my email");
                break;
            case "delete.account":
                subject = "Account Deleted";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "We have received your account deletion request. If you did not initiate this action, please contact our support immediately.");
                break;
            case "invoice.paid":
                subject = "Invoice Payment Confirmation";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "Your payment has been received successfully. Thank you for your prompt response.");
                // Button
                button.put("label", "Access to your customer portal");
                break;
            case "invoice.payment_failed":
                subject = "Payment Issue Notification";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "We encountered an issue processing your payment. Please review your payment details or contact our support team for assistance.");
                // Button
                button.put("label", "Access to your customer portal");
                break;
            case "invoice.finalized":
                subject = "Invoice Finalized";
                email.addPersonalization(recipient, "subject",subject);
                email.addPersonalization(recipient, "message", "Your invoice has been finalized. Please check your account for details. If you have any questions, feel free to reach out to us.");
                // Button
                button.put("label", "Download your billing");
                break;
            case "customer.updated":
                subject ="Billing information updated";
                email.addPersonalization(recipient, "subject", subject );
                email.addPersonalization(recipient, "message", "You made the updates on your billing information on Customer portal. If you have any questions, feel free to reach out to us.");

                // Button
                button.put("label", "Access to your customer portal");
                break;
            case "customer.subscription.created":
                subject = "Customer Subscription Created";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "Thank you for subscribing to our service. Your subscription is now active. We hope you enjoy the benefits of being with us.");
                // Button
                button.put("label", "Access to your customer portal");
                break;
            case "customer.subscription.deleted":
                subject ="Subscription Cancellation Notice";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "Your subscription has been canceled as per your request. If this was a mistake, please contact our support team immediately.");
                // Button
                button.put("label", "Access to your customer portal");
                break;
            case "customer.subscription.updated":
                subject = "Customer Subscription Updated";
                email.addPersonalization(recipient, "subject", subject);
                email.addPersonalization(recipient, "message", "Your subscription details have been updated successfully. Please review the changes in your account at your convenience.");
                // Button
                button.put("label", "Access to your customer portal");
                break;
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
        }

    }
}
