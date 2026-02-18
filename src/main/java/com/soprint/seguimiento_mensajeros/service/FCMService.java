package com.soprint.seguimiento_mensajeros.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FCMService {

    public String sendNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            return "Token is empty";
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            return FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending notification: " + e.getMessage();
        }
    }
}
