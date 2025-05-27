package com.cyna.subscriptions.services;

public interface IEmailService {
    void sendEmail(String to, String link, String eventType);
}