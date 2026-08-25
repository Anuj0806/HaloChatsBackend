-- ============================================================
-- Group chat schema changes
--
-- Run this ONCE against an existing database before deploying the
-- group-chat release. New databases don't need it: ddl-auto=update
-- creates everything correctly from scratch.
--
-- There is no Flyway/Liquibase in this project, so this file is not
-- executed automatically. Apply it with psql:
--
--   psql -d chatdb -f src/main/resources/db/migration/V2__group_chat.sql
-- ============================================================

-- 1. A group message becomes one row per recipient, all sharing the
--    sender's message_id. The old single-column unique index would
--    reject every recipient after the first.
--
--    The index name below is Hibernate's default for a column-level
--    unique constraint. Check yours first if it differs:
--      \d chat_messages
ALTER TABLE chat_messages DROP CONSTRAINT IF EXISTS chat_messages_message_id_key;
DROP INDEX IF EXISTS chat_messages_message_id_key;

ALTER TABLE chat_messages
    ADD CONSTRAINT uk_chat_message_recipient UNIQUE (message_id, receiver);

-- 2. New columns. ddl-auto=update would add these itself, but doing it
--    here keeps the whole change in one reviewable place.
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS group_id VARCHAR(64);
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS sender_name VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_chat_messages_receiver_status
    ON chat_messages (receiver, status);

-- 3. Group tables.
CREATE TABLE IF NOT EXISTS chat_groups (
    id          BIGSERIAL PRIMARY KEY,
    group_id    VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(80)  NOT NULL,
    avatar_id   VARCHAR(40),
    created_by  VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS group_members (
    id           BIGSERIAL PRIMARY KEY,
    group_id     VARCHAR(64)  NOT NULL,
    member_phone VARCHAR(255) NOT NULL,
    role         VARCHAR(16)  NOT NULL,
    joined_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uk_group_member UNIQUE (group_id, member_phone)
);

CREATE INDEX IF NOT EXISTS idx_group_members_group ON group_members (group_id);
CREATE INDEX IF NOT EXISTS idx_group_members_phone ON group_members (member_phone);


-- ============================================================
-- Sealed (end-to-end encrypted) chat
--
-- Only PUBLIC keys are stored. Private keys are generated in the
-- browser and never transmitted, which is what makes sealed chat
-- end-to-end - the server relays ciphertext it cannot read.
-- ============================================================

CREATE TABLE IF NOT EXISTS user_public_keys (
    id          BIGSERIAL PRIMARY KEY,
    phone       VARCHAR(255) NOT NULL UNIQUE,
    public_key  TEXT         NOT NULL,
    fingerprint VARCHAR(128) NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);
