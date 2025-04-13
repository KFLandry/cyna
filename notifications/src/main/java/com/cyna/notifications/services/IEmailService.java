package com.cyna.notifications.services;

public interface IEmailService {
    void sendEmail(String to, String subject, String text);
    void sendEmail(String to, String subject, String text, String attachment);
    void sendEmail(String to, String subject, String text, String attachement, String templateType);
}
