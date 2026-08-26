package com.example.chatapp.service.emailService;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from}")
    private String fromEmail;

    public void sendOtpEmail(
            String toEmail,
            String subject,
            String name,
            String otp
    ) {

        try {

            System.out.println("=================================");
            System.out.println("Starting OTP email");
            System.out.println("To: " + toEmail);
            System.out.println("From: " + fromEmail);
            System.out.println("API key configured: "
                    + (resendApiKey != null && !resendApiKey.isBlank()));
            System.out.println("=================================");

            Resend resend = new Resend(resendApiKey);

            String html = """
                    <html>
                    <body>
                        <h2>Hello %s</h2>
                        <p>Your Halo Chat verification code is:</p>

                        <h1>%s</h1>

                        <p>This OTP expires in 10 minutes.</p>

                        <p>If you did not request this code, ignore this email.</p>
                    </body>
                    </html>
                    """.formatted(name, otp);

            CreateEmailOptions params =
                    CreateEmailOptions.builder()
                            .from(fromEmail)
                            .to(toEmail)
                            .subject(subject)
                            .html(html)
                            .build();

            CreateEmailResponse response =
                    resend.emails().send(params);

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "RESEND EMAIL SUCCESS"
            );

            System.out.println(
                    "Resend ID: " + response.getId()
            );

            System.out.println(
                    "================================="
            );

        } catch (Exception e) {

            System.err.println(
                    "================================="
            );

            System.err.println(
                    "RESEND EMAIL FAILED"
            );

            e.printStackTrace();

            System.err.println(
                    "================================="
            );

            throw new RuntimeException(
                    "Failed to send OTP email to " + toEmail,
                    e
            );
        }
    }
}