package com.cyna.orders.services;

public interface IEmailService {
    void sendEmail(String to, String link, String eventType);
}
