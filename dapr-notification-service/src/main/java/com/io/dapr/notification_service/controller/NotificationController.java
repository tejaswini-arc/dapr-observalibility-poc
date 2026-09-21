package com.io.dapr.notification_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/*

Normal request
POST /api/notifications

Timeout simulation
POST /api/notifications?delay=10000                    //The service sleeps for 10 seconds.

Failure simulation
POST /api/notifications?fail=true                      //The service throws an exception.
 */

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {


        /** This counter keeps track of how many times the notification endpoint has been called during the transient-failure experiment.
         ** Why do we need it?
         ** We want to demonstrate:
         *     Attempt 1 -> FAIL
         *     Attempt 2 -> FAIL
         *     Attempt 3 -> SUCCESS
         *
         * Dapr is responsible for performing the retries.This counter allows our application to know which retry attempt, it is currently processing.
         *
         * AtomicInteger is used instead of a normal int because it is thread-safe when multiple requests are processed concurrently.
         *
         * Example:
         *
         *     Initial value = 0
         *     Request 1    -> 1
         *     Request 2    -> 2
         *     Request 3    -> 3
         */
        private final AtomicInteger transientFailureCounter = new AtomicInteger(0);


        /* * The endpoint supports three query parameters:
         *
         * delay=true/false  ?delay=10000 defaultValue = "0" means that if the caller doesn't  provide delay, no delay is introduced.
                             -> actually delay is a number of milliseconds
         * fail=true/false   -> permanently simulate a failure,means the notification service will intentionally throw an exception and return HTTP 500.
                             If fail is not supplied, it defaults to false.
         * transientFailure=true/false           -> fail first two attempts and then succeed
         *                                         ?transientFailure=true

                 * means:
                 *
                 *     Attempt 1 -> FAIL
                 *     Attempt 2 -> FAIL
                 *     Attempt 3 -> SUCCESS
                 *
                 * If it is not supplied, it defaults to false.
         */
        @PostMapping
        public String sendNotification(@RequestParam(defaultValue = "0") long delay,@RequestParam(defaultValue = "false") boolean fail, @RequestParam(defaultValue = "false") boolean transientFailure) {

            System.out.println("Notification service received request");

            if (delay > 0) {

                System.out.println("Simulating delay: " + delay + " ms");


                try {

                    Thread.sleep(delay);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    /* Convert the interruption into an application
                     * exception so the request doesn't continue normally.
                     */
                    throw new RuntimeException(
                            "Notification interrupted", e);
                }
            }



            if (fail) {

                System.out.println(
                        "Simulating notification failure");

                throw new RuntimeException(
                        "Notification service simulated failure");
            }



            if (transientFailure) {


                /*
                 * Increment the counter and return the new value.
                 *
                 * Because it starts at 0:
                 *
                 * First request  -> 1
                 * Second request -> 2
                 * Third request  -> 3
                 */
                int attempt = transientFailureCounter.incrementAndGet();
                System.out.println("Transient failure experiment - attempt: "+ attempt);


                /*
                 * Intentionally fail the first two attempts.
                 *
                 * attempt = 1 -> 1 <= 2 -> TRUE -> FAIL
                 *
                 * attempt = 2 -> 2 <= 2 -> TRUE -> FAIL
                 *
                 * attempt = 3 -> 3 <= 2 -> FALSE -> continue
                 */
                if (attempt <= 2) {

                    throw new RuntimeException("Transient notification failure on attempt " + attempt);
                }


                /*
                 * We reach this point only after the first two
                 * attempts have failed.
                 *
                 * Therefore attempt 3 should succeed.
                 */
                System.out.println("Transient failure recovered on attempt: "+ attempt);
                return "Notification sent successfully on attempt "+ attempt;
            }

            return "Notification sent successfully";
        }
    }

