package com.infiniteprints.platform.ecommerce.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleMailService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.refresh.token}")
    private String refreshToken;

    @Value("${google.sender.email}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtpEmail(
            String recipientEmail,
            String otp
    ) {

        try {

            String accessToken = generateAccessToken();

            String subject = "Verify your account";

            String body =
                    "Your OTP is: "
                            + otp
                            + "\n\n"
                            + "This OTP expires in 5 minutes.";

            String rawEmail =
                    "From: " + senderEmail + "\r\n"
                            + "To: " + recipientEmail + "\r\n"
                            + "Subject: " + subject + "\r\n"
                            + "\r\n"
                            + body;

            String encodedEmail = Base64
                    .getUrlEncoder()
                    .encodeToString(
                            rawEmail.getBytes(StandardCharsets.UTF_8)
                    );

            String gmailApiUrl =
                    "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

            HttpHeaders headers = new HttpHeaders();

            headers.setBearerAuth(accessToken);

            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of(
                    "raw",
                    encodedEmail
            );

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(
                            requestBody,
                            headers
                    );

            restTemplate.exchange(
                    gmailApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Failed to send OTP email",
                    exception
            );
        }
    }

    private String generateAccessToken() {

        String tokenUrl =
                "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.put(
                "client_id",
                List.of(clientId)
        );

        body.put(
                "client_secret",
                List.of(clientSecret)
        );

        body.put(
                "refresh_token",
                List.of(refreshToken)
        );

        body.put(
                "grant_type",
                List.of("refresh_token")
        );

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        tokenUrl,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

        Map responseBody = response.getBody();

        if (
                responseBody == null
                        || !responseBody.containsKey("access_token")
        ) {

            throw new RuntimeException(
                    "Failed to generate access token"
            );
        }

        return responseBody
                .get("access_token")
                .toString();
    }
}