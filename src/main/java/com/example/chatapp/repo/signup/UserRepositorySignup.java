package com.example.chatapp.repo.signup;

import com.example.chatapp.DTO.UserDTO;
import com.example.chatapp.entity.UserSignup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepositorySignup extends JpaRepository<UserSignup, Long> {
    Optional<UserSignup> findByEmail(String email);

    Optional<UserSignup> findByEmailIgnoreCase(String email);

    Optional<UserSignup> findByPhoneNumber(String phoneNumber);

    List<UserSignup> findByPhoneNumberIn(Collection<String> phoneNumbers);

    Optional<UserSignup> findByPublicUserId(String publicUserId);

    @Query("""
            SELECT u FROM UserSignup u
            WHERE u.email = :value OR u.phoneNumber = :value
            """)
    Optional<UserSignup> findByEmailOrPhoneNumber(@Param("value") String value);

    @Query("SELECT new com.example.chatapp.DTO.UserDTO(u.name, u.phoneNumber, u.email, u.avatarId, u.about) " +
            " FROM UserSignup u WHERE u.phoneNumber = :phone")
    Optional<UserDTO> getUserDTOByPhone(@Param("phone") String phone);

    @Query("SELECT new com.example.chatapp.DTO.UserDTO(u.name, u.phoneNumber, u.email, u.avatarId, u.about) " +
            " FROM UserSignup u WHERE u.isVerified = true AND u.phoneNumber <> :excludePhone AND (" +
            " LOWER(u.name) LIKE LOWER(CONCAT('%', :term, '%')) OR u.phoneNumber LIKE CONCAT('%', :term, '%'))")
    List<UserDTO> searchDirectory(@Param("term") String term,
                                  @Param("excludePhone") String excludePhone);
}
