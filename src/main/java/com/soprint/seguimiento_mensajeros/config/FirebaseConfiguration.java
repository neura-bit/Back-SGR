package com.soprint.seguimiento_mensajeros.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class FirebaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

    /**
     * Inicializa Firebase si hay credenciales disponibles.
     * Si no las hay (entornos de desarrollo local), la aplicación arranca igual y
     * solo quedan deshabilitadas las notificaciones push; FCMService ya devuelve un
     * error controlado cuando Firebase no está inicializado.
     */
    @Bean
    public FirebaseApp firebaseApp() {
        try {
            InputStream serviceAccount = getServiceAccountStream();

            if (serviceAccount == null) {
                log.warn("Firebase no se inicializó: no se encontró 'serviceAccountKey.json' en el classpath " +
                        "ni la variable de entorno FIREBASE_SERVICE_ACCOUNT_BASE64. " +
                        "Las notificaciones push quedarán deshabilitadas.");
                return null;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            } else {
                return FirebaseApp.getInstance();
            }
        } catch (Exception e) {
            log.warn("Firebase no se pudo inicializar ({}). Las notificaciones push quedarán deshabilitadas.",
                    e.getMessage());
            return null;
        }
    }

    /**
     * @return el stream de credenciales, o null si no hay ninguna configurada.
     */
    private InputStream getServiceAccountStream() throws java.io.IOException {
        String base64Key = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");

        if (base64Key != null && !base64Key.isEmpty()) {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Key);
            return new java.io.ByteArrayInputStream(decodedBytes);
        }

        ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
        if (!resource.exists()) {
            return null;
        }
        return resource.getInputStream();
    }
}
