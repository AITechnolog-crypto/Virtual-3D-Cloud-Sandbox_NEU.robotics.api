package com.june.geospatial.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Ein simulierter E-Mail-Service, der E-Mails in der Konsole ausgibt.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendEmail(String recipient, String subject, String body) {
        logger.info("📧 Sende E-Mail (simuliert)...");
        logger.info("   -> An: {}", recipient);
        logger.info("   -> Betreff: {}", subject);
        logger.info("   -> Inhalt: \n{}", body);
    }
}
