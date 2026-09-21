package com.io.dapr.payment_service.controller;

import com.io.dapr.payment_service.service.DaprNotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final DaprNotificationService notificationService;

    public PaymentController(
            DaprNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public String makePayment() {

        System.out.println("Payment received");
        String notificationResponse = notificationService.sendNotification();
        System.out.println("Notification response: " + notificationResponse);
        return "Payment processed successfully. "
                + notificationResponse;
    }
}