package com.soprint.seguimiento_mensajeros.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfiguration {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        InputStream serviceAccount = getServiceAccountStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    private InputStream getServiceAccountStream() throws IOException {
        String base64Key = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");

        if (base64Key != null && !base64Key.isEmpty()) {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Key);
            return new java.io.ByteArrayInputStream(decodedBytes);
        } else {
            ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
            return resource.getInputStream();
        }
    }
}
