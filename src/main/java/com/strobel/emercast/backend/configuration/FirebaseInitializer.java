package com.strobel.emercast.backend.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseInitializer.class);

    @PostConstruct
    public void onStart() {
        logger.info("Initializing Firebase App...");
        try {
            this.initializeFirebaseApp();
        } catch (IOException e) {
            logger.error("Initializing Firebase App", e);
        }
    }

    private void initializeFirebaseApp() throws IOException {

        if (FirebaseApp.getApps() == null || FirebaseApp.getApps().isEmpty()) {

            var firebaseOptionBuilder = new FirebaseOptions.Builder();

            // This isn't needed for dev because the "FIRESTORE_EMULATOR_HOST" and "FIREBASE_AUTH_EMULATOR_HOST" so local emulator version of firebase services are used
            // For production there needs to be a "firebase-service-credentials.json" file in the resource folder that can be obtained from the firebase web interface
            InputStream serviceAccount = FirebaseInitializer.class.getResourceAsStream("/firebase-service-credentials.json");
            if(serviceAccount == null) {
                throw new AssertionError("Error: firebase-service-credentials.json is missing from resources. Get it from firebase web interface");
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            firebaseOptionBuilder.setCredentials(credentials);
            logger.info("Connected to production to production firebase project");

            FirebaseApp.initializeApp(firebaseOptionBuilder.build());
        }

    }

}