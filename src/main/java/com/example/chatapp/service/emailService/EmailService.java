package com.example.chatapp.service.emailService;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final SpringTemplateEngine templateEngine;

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from}")
    private String fromEmail;

    @Async
    @Retryable(
            retryFor = RuntimeException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void sendOtpEmail(
            String toEmail,
            String subject,
            String name,
            String otp
    ) {

        try {

            // Create Resend client
            Resend resend = new Resend(resendApiKey);

            // Thymeleaf context
            Context context = new Context();

            context.setVariable("name", name);
            context.setVariable("expiryMinutes", 10);
            context.setVariable("otp", otp);

            // Process Thymeleaf HTML template
            String htmlContent =
                    templateEngine.process("otp-git .html", context);

            // Create email
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            // Send email through Resend
            CreateEmailResponse response =
                    resend.emails().send(params);

            System.out.println(
                    "OTP email sent successfully. Resend ID: "
                            + response.getId()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to send OTP email to " + toEmail,
                    e
            );
        }
    }
}

