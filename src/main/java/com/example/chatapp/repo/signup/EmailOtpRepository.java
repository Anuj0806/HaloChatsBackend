package com.example.chatapp.repo.signup;

import com.example.chatapp.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByUserSignup_EmailAndOtpCodeAndUsedFalseOrderByIdDesc(
            String email,
            String otpCode
    );

    @Modifying
    @Query("UPDATE EmailOtp e SET e.used = true WHERE e.userSignup.email = :email AND e.used = false")
    void invalidateOldOtps(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM EmailOtp e WHERE e.userSignup.email = :email")
    void deleteAllByUserEmail(@Param("email") String email);
}
