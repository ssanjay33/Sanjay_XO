package com.xo.eventmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends verification emails via Brevo's HTTPS transactional email API.
 * (Render's free tier blocks outbound SMTP ports 25/465/587 entirely, so
 * a plain JavaMailSender/SMTP approach will always time out there - an
 * HTTPS-based API like Brevo's is the reliable free-tier-compatible option.)
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${xo.app.brevo.apiKey:}")
    private String apiKey;

    @Value("${xo.app.brevo.senderEmail:}")
    private String senderEmail;

    @Value("${xo.app.brevo.senderName:XO Events}")
    private String senderName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendVerificationCode(String toEmail, String name, String code) {
        if (apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            logger.warn(">> [DEV MODE - EMAIL NOT CONFIGURED] Verification code for {} ({}): {}",
                    name, toEmail, code);
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            payload.put("sender", sender);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            recipient.put("name", name);
            payload.put("to", List.of(recipient));

            payload.put("subject", "XO Events - Verify Your Email");
            payload.put("textContent",
                    "Hi " + name + ",\n\n" +
                    "Welcome to XO Event Management! Please use the verification code below to activate your account:\n\n" +
                    "    " + code + "\n\n" +
                    "This code expires in 15 minutes. If you did not sign up for XO Events, you can safely ignore this email.\n\n" +
                    "- The XO Events Team");

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Verification email sent to {} via Brevo", toEmail);
            } else {
                logger.error("Brevo API returned {} for {}: {}", response.statusCode(), toEmail, response.body());
                logger.warn(">> [EMAIL SEND FAILED - FALLBACK] Verification code for {} ({}): {}",
                        name, toEmail, code);
            }
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            logger.warn(">> [EMAIL SEND FAILED - FALLBACK] Verification code for {} ({}): {}",
                    name, toEmail, code);
        }
    }
}
