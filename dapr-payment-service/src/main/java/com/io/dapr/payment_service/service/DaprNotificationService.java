package com.io.dapr.payment_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DaprNotificationService {

    private final RestClient restClient;

    public DaprNotificationService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:3501")
                .build();
    }

    public String sendNotification() {

        return restClient.post()
                .uri("/v1.0/invoke/notification-service/method/api/notifications")
                .retrieve()
                .body(String.class);
    }
}