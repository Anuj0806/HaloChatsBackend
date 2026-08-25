package com.example.chatapp.repo.chat;

import com.example.chatapp.entity.chat.UserPublicKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPublicKeyRepository extends JpaRepository<UserPublicKey, Long> {

    Optional<UserPublicKey> findByPhone(String phone);

    List<UserPublicKey> findByPhoneIn(Collection<String> phones);

    /*
    Publishing a key used to be "find the row, or build a new one, then
    save" - a read followed by a write that are not atomic together.
    Two requests for the same phone number arriving close enough
    together - React StrictMode double-invoking an effect in
    development, a second open tab, a retried request - can both read
    "no row yet" and both attempt to insert one. The loser's INSERT
    then violates the unique constraint on `phone` and surfaces as a
    raw database error instead of the successful "key published"
    response it should have gotten.

    ON CONFLICT makes the whole operation one atomic statement:
    Postgres itself serialises concurrent upserts to the same row, so
    there is no window in which two requests can both believe they're
    the one inserting. This removes the race rather than working
    around it with catch-and-retry logic operating on a transaction
    that may already be poisoned by the failed flush.
    */
    @Modifying
    @Query(
            value = """
                    INSERT INTO user_public_keys (phone, public_key, fingerprint, updated_at)
                    VALUES (:phone, :publicKey, :fingerprint, CURRENT_TIMESTAMP)
                    ON CONFLICT (phone)
                    DO UPDATE SET
                        public_key = EXCLUDED.public_key,
                        fingerprint = EXCLUDED.fingerprint,
                        updated_at = CURRENT_TIMESTAMP
                    """,
            nativeQuery = true
    )
    void upsert(
            @Param("phone") String phone,
            @Param("publicKey") String publicKey,
            @Param("fingerprint") String fingerprint
    );
}
