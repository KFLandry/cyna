package com.cyna.auth_users.users.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("local")
@Service
@Slf4j
public class MailerSendServiceMock implements IEmailService {

    @PostConstruct
    public void init() {
        log.warn("⚠️ Bean MailerSendServiceMock initialisé (profil local actif)");
    }

    @Override
    public void sendEmail(String to, String link, String eventType) {
        log.warn("[FAKE EMAIL - 3 params] to={} | link={} | eventType={}", to, link, eventType);
    }

    @Override
    public void sendEmail(String to, String link, String eventType, String requester) {
        log.warn("[FAKE EMAIL - 4 params] to={} | link={} | eventType={} | requester={}", to, link, eventType, requester);
    }
}
