package com.example.chatapp.service.appUpdate;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class ApkHashService {

    public String calculateSha256(InputStream apkStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;

            while ((read = apkStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }

            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate APK hash", e);
        }
    }
}
