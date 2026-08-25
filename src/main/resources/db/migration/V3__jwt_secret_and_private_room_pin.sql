-- ============================================================
-- Private Room PIN
--
-- A pure access-control gate in front of the Private Room tab -
-- never used as cryptographic key material. See PrivateRoomPin's
-- Javadoc in the entity class for why.
--
-- Nothing to migrate for the JWT signing key fix (JwtUtil.java) -
-- it's a property, not a schema change. Set app.security.jwt-secret
-- in application.properties or as an environment variable before
-- your next deploy so existing sessions survive it.
-- ============================================================

CREATE TABLE IF NOT EXISTS private_room_pins (
    id              BIGSERIAL PRIMARY KEY,
    phone           VARCHAR(255) NOT NULL UNIQUE,
    pin_hash        VARCHAR(255) NOT NULL,
    failed_attempts INTEGER      NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL
);
