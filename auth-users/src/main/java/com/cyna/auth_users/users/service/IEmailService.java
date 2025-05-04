package com.cyna.auth_users.users.service;

public interface IEmailService {
    void sendEmail(String to, String link, String eventType);
}
