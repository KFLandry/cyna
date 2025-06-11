package com.cyna.auth_users.users.service;

public interface IEmailService {
    void sendEmail(String to, String link, String eventType) throws Exception;
    // Surcharge pour AuthService
    void sendEmail(String to, String link, String eventType, String requester) throws Exception;
}